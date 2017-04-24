package scan

import java.io.IOException
import java.nio.file._

import scala.compat.java8.StreamConverters._
import scala.collection.SortedSet

import cats._
import cats.data._
import cats.implicits._

import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

import org.atnos.eff.addon.monix._
import org.atnos.eff.addon.monix.task._
import org.atnos.eff.syntax.addon.monix.task._

import monix.eval._

import EffTypes._

import monix.execution.Scheduler.Implicits.global

object Scanner {

  type R = Fx.fx5[Reader[Filesystem, ?], Reader[ScanConfig, ?], Either[Throwable, ?], Task, Writer[Log, ?]]

  def main(args: Array[String]): Unit = {
    scanReport(Directory(args(0)), 10).map(r => {
      val (result, logs) = r
      println(logs.mkString("\n"))
      println(result)
    }).runAsync
  }

  def scanReport(base: FilePath, topN: Int): Task[(String, List[Log])] = for {
    start <- Task.eval(System.currentTimeMillis)

    //unfortunately Scala doesn't handle pattern matching of tuples in for-expressions well
    //this two stage form allows the parts of the pair to be named
    s <- pathScan(base, topN, DefaultFilesystem)
    (result, logs) = s

  } yield (result match {
    case Right(scan) => ReportFormat.largeFilesReport(scan, base.path) +
      s"\nElapsed ${System.currentTimeMillis - start}ms"
    case Left(ex) => s"Scan of '${base.path}' failed: $ex"
  }, logs)

  def pathScan(base: FilePath, topN: Int, fs: Filesystem): Task[(Either[Throwable, PathScan], List[Log])] = {
    //build an Eff program (ie a data structure)
    val effScan: Eff[R, PathScan] = tell[R, Log](Log.info(s"Scan started on $base")) >> PathScan.scan[R](base)

    //execute the Eff expression by interpreting it
    effScan.runReader(ScanConfig(topN)).runReader(fs).runEither.runWriter.runAsync
  }
}

object EffTypes {

  type _filesystem[R] = Reader[Filesystem, ?] <= R
  type _config[R] = Reader[ScanConfig, ?] <= R
  type _log[R] = Writer[Log, ?] <= R
}

sealed trait Log {def msg: String}
object Log {
  def error: String => Log = Error(_)
  def info: String => Log = Info(_)
  def debug: String => Log = Debug(_)
}
case class Error(msg: String) extends Log
case class Info(msg: String) extends Log
case class Debug(msg: String) extends Log

sealed trait FilePath {
  def path: String
}
case class File(path: String) extends FilePath
case class Directory(path: String) extends FilePath

trait Filesystem {

  @throws(classOf[IOException])
  def length(file: File): Long

  @throws(classOf[IOException])
  def listFiles(directory: Directory): List[FilePath]

}
case object DefaultFilesystem extends Filesystem {

  def length(file: File) = Files.size(Paths.get(file.path))

  def listFiles(directory: Directory) = {
    val files = Files.list(Paths.get(directory.path))
    try files.toScala[List].flatMap {
        case dir if Files.isDirectory(dir) => List(Directory(dir.toString))
        case file if Files.isRegularFile(file) => List(File(file.toString))
        case _ => List.empty
      }
    finally files.close()
  }
}

case class ScanConfig(topN: Int)

case class PathScan(largestFiles: SortedSet[FileSize], totalSize: Long, totalCount: Long)

object PathScan {

  def empty: PathScan = PathScan(SortedSet.empty, 0, 0)

    def scan[R: _filesystem: _config: _throwableEither: _Task: _log](path: FilePath): Eff[R, PathScan] = path match {
    case file: File =>
      for {
        fs <- FileSize.ofFile(file)
      }
      yield PathScan(SortedSet(fs), fs.size, 1)
    case dir: Directory =>
      for {
        fs <- ask[R, Filesystem]
        topN <- PathScan.takeTopN
        files <- catchNonFatalThrowable(fs.listFiles(dir))
        _ <- {
          val dirCount = files.count(_.isInstanceOf[Directory])
          val fileCount = files.count(_.isInstanceOf[File])
          tell(Log.debug(s"Scanning directory '$dir': $dirCount subdirectories and $fileCount files"))
        }
        concurrentChildScans <- Eff.traverseA(files)(file => taskSuspend(Task.eval(PathScan.scan[R](file))))
      }
      yield concurrentChildScans.combineAll(topN)
  }

  def takeTopN[R: _config]: Eff[R, Monoid[PathScan]] = for {
    scanConfig <- ask
  } yield new Monoid[PathScan] {
    def empty: PathScan = PathScan.empty

    def combine(p1: PathScan, p2: PathScan): PathScan = PathScan(
      p1.largestFiles.union(p2.largestFiles).take(scanConfig.topN),
      p1.totalSize + p2.totalSize,
      p1.totalCount + p2.totalCount
    )
  }

}

case class FileSize(path: File, size: Long)

object FileSize {

  def ofFile[R: _filesystem: _throwableEither](file: File): Eff[R, FileSize] = for {
    fs <- ask
    size <- catchNonFatalThrowable(fs.length(file))
  } yield FileSize(file, size)

  implicit val ordering: Ordering[FileSize] = Ordering.by[FileSize, Long  ](_.size).reverse
}

object ReportFormat {

  def largeFilesReport(scan: PathScan, rootDir: String): String = {
    if (scan.largestFiles.nonEmpty) {
      s"Largest ${scan.largestFiles.size} file(s) found under path: $rootDir\n" +
        scan.largestFiles.map(fs => s"${(fs.size * 100)/scan.totalSize}%  ${formatByteString(fs.size)}  ${fs.path}").mkString("", "\n", "\n") +
        s"${scan.totalCount} total files found, having total size ${formatByteString(scan.totalSize)} bytes.\n"
    }
    else
      s"No files found under path: $rootDir"
  }

  def formatByteString(bytes: Long): String = {
    if (bytes < 1000)
      s"${bytes} B"
    else {
      val exp = (Math.log(bytes) / Math.log(1000)).toInt
      val pre = "KMGTPE".charAt(exp - 1)
      s"%.1f ${pre}B".format(bytes / Math.pow(1000, exp))
    }
  }
}
