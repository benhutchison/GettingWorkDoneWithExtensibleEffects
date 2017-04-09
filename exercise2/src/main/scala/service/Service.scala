package service

import cats._
import cats.data.Reader
import cats.implicits._
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._
import service.PathScan._filesystem

import scala.collection.SortedSet

object Service {

  type R = Fx.fx2[Reader[Filesystem, ?], Reader[ScanConfig, ?]]

  def main(args: Array[String]): Unit = {
    val rootDir = new Directory(args(0))

    //build an Eff program (ie a data structure)
    val effScan: Eff[R, PathScan] = PathScan.scan[R](rootDir)

    //execute the Eff expression by interpreting it
    val scan = effScan.runReader(ScanConfig(10)).runReader(DefaultFilesystem: Filesystem).run

    println(largeFilesReport(scan, rootDir))
  }

  def largeFilesReport(scan: PathScan, rootDir: FilePath): String = {
    if (scan.largestFiles.nonEmpty) {
      s"Largest ${scan.largestFiles.size} file(s) found under path: ${rootDir.path}\n" +
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

sealed trait FilePath {
  def path: String
}
case class File(path: String) extends FilePath
case class Directory(path: String) extends FilePath

trait Filesystem {

  def length(file: File): Long

  def listFiles(directory: Directory): Option[Array[File]]

}
case object DefaultFilesystem extends Filesystem {

  def length(file: File) = new java.io.File(file.path).length()

  def listFiles(directory: Directory) = Option(new java.io.File(directory.path).listFiles()).map(
    _.map(javaFile => File(javaFile.getPath)))

}

case class ScanConfig(topN: Int)

case class PathScan(largestFiles: SortedSet[FileSize], totalSize: Long, totalCount: Long)

object PathScan {

  type _filesystem[R] = Reader[Filesystem, ?] <= R
  type _config[R] = Reader[ScanConfig, ?] <= R

  def empty: PathScan = PathScan(SortedSet.empty, 0, 0)

  def scan[R: _filesystem: _config](path: FilePath): Eff[R, PathScan] = path match {
    case file: File =>
      for {
        fs <- FileSize.ofFile(file)
      }
      yield PathScan(SortedSet(fs), fs.size, 1)
    case dir: Directory =>
      for {
        fs <- ask[R, Filesystem]
        topN <- PathScan.takeTopN
        childScans <- fs.listFiles(dir) match {
           case Some(children) =>
             children.toList.foldMapM(PathScan.scan[R](_))(Monad[Eff[R, ?]], topN)
           case None =>
             PathScan.empty.pureEff[R]
        }
      } yield childScans
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

  def ofFile[R: _filesystem](file: File): Eff[R, FileSize] = for {
    fs <- ask
  } yield FileSize(file, fs.length(file))

  implicit val ordering: Ordering[FileSize] = Ordering.by[FileSize, Long  ](_.size).reverse

}
