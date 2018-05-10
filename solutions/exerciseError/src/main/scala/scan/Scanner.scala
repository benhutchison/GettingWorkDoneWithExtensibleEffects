package scan

import java.nio.file._

import scala.compat.java8.StreamConverters._
import scala.collection.SortedSet

import cats._
import cats.data._
import cats.implicits._

import mouse.all._

import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

import org.atnos.eff.addon.monix._
import org.atnos.eff.addon.monix.task._
import org.atnos.eff.syntax.addon.monix.task._

import monix.eval._
import monix.execution._

import EffTypes._

import scala.concurrent.duration._


object Scanner {
  val Usage = "Scanner <path> [number of largest files to track]"

  type R = Fx.fx3[Task, Reader[Filesystem, ?], Either[String, ?]]

  implicit val s = Scheduler(ExecutionModel.BatchedExecution(32))

  def main(args: Array[String]): Unit = {
    val program = scanReport[R](args).map(println)
    
    program.runReader(DefaultFilesystem: Filesystem).runEither.runAsync.attempt.runSyncUnsafe(1.minute).leftMap(_.toString).flatten
  }

  def scanReport[R: _task: _filesystem: _err](args: Array[String]): Eff[R, String] = for {
    base <- optionEither(args.lift(0), s"Path to scan must be specified.\n$Usage")

    topN <- {
      val n = args.lift(1).getOrElse("10")
      fromEither(n.parseInt.leftMap(_ => s"Number of files must be numeric: $n"))
    }
    topNValid <- if (topN < 0) left[R, String, Int](s"Invalid number of files $topN") else topN.pureEff[R]

    fs <- ask[R, Filesystem]

    scan <- pathScan[Fx.prepend[Reader[ScanConfig, ?], R]](
      fs.filePath(base)).runReader[ScanConfig](ScanConfig(topNValid))

  } yield ReportFormat.largeFilesReport(scan, base.toString)

  def pathScan[R: _task: _filesystem: _config](path: FilePath): Eff[R, PathScan] = path match {
    case f: File =>
      for {
        fs <- FileSize.ofFile(f)
      } yield PathScan(SortedSet(fs), fs.size, 1)
    case dir: Directory =>
      for {
        filesystem <- ask[R, Filesystem]
        topN <- takeTopN
        fileList <- taskDelay(filesystem.listFiles(dir))
        childScans <- fileList.traverse(pathScan[R](_))
      } yield childScans.combineAll(topN)
    case Other(_) =>
      PathScan.empty.pureEff[R]
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

trait Filesystem {

  def filePath(path: String): FilePath

  def length(file: File): Long

  def listFiles(directory: Directory): List[FilePath]

}
case object DefaultFilesystem extends Filesystem {

  def filePath(path: String): FilePath =
    if (Files.isRegularFile(Paths.get(path)))
      File(path.toString)
    else if (Files.isDirectory(Paths.get(path)))
      Directory(path)
    else
      Other(path)

  def length(file: File) = Files.size(Paths.get(file.path))

  def listFiles(directory: Directory) = {
    val files = Files.list(Paths.get(directory.path))
    try files.toScala[List].flatMap(path => filePath(path.toString) match {
      case Directory(path) => List(Directory(path))
      case File(path) => List(File(path))
      case Other(path) => List.empty
    })
    finally files.close()
  }

}

case class ScanConfig(topN: Int)

case class PathScan(largestFiles: SortedSet[FileSize], totalSize: Long, totalCount: Long)

object PathScan {

  def empty = PathScan(SortedSet.empty, 0, 0)

}

case class FileSize(file: File, size: Long)

object FileSize {

  def ofFile[R: _filesystem](file: File): Eff[R, FileSize] = for {
    fs <- ask
  } yield  FileSize(file, fs.length(file))

  implicit val ordering: Ordering[FileSize] = Ordering.by[FileSize, Long](_.size).reverse

}

object EffTypes {

  type _filesystem[R] = Reader[Filesystem, ?] <= R
  type _config[R] = Reader[ScanConfig, ?] <= R
  type _err[R] = Either[String, ?] <= R
}


//I prefer an closed set of disjoint cases over a series of isX(): Boolean tests, as provided by the Java API
//The problem with boolean test methods is they make it unclear what the complete set of possible states is, and which tests
//can overlap
sealed trait FilePath {
  def path: String
}

case class File(path: String) extends FilePath
case class Directory(path: String) extends FilePath
case class Other(path: String) extends FilePath

//Common pure code that is unaffected by the migration to Eff
object ReportFormat {

  def largeFilesReport(scan: PathScan, rootDir: String): String = {
    if (scan.largestFiles.nonEmpty) {
      s"Largest ${scan.largestFiles.size} file(s) found under path: $rootDir\n" +
        scan.largestFiles.map(fs => s"${(fs.size * 100)/scan.totalSize}%  ${formatByteString(fs.size)}  ${fs.file}").mkString("", "\n", "\n") +
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
