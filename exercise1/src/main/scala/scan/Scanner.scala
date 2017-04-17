package scan

import java.nio.file._

import scala.compat.java8.StreamConverters._
import scala.collection.SortedSet

import cats._
import cats.implicits._


object Scanner {

  def main(args: Array[String]): Unit = {
    println(scanReport(Paths.get(args(0)), 10))
  }

  def scanReport(base: Path, topN: Int): String = {
    val scan = pathScan(base, topN)

    ReportFormat.largeFilesReport(scan, base.toString)
  }

  def pathScan(path: Path, topN: Int): PathScan = path match {
    case file if Files.isRegularFile(path) =>
      val fs = FileSize.ofFile(path)
      PathScan(SortedSet(fs), fs.size, 1)
    case dir if Files.isDirectory(path) =>
        Files.list(dir).toScala[Stream].foldMap(pathScan(_, topN))(PathScan.topNMonoid(topN))
    case _ =>
      PathScan.empty
  }

}

case class PathScan(largestFiles: SortedSet[FileSize], totalSize: Long, totalCount: Long)

object PathScan {

  def empty = PathScan(SortedSet.empty, 0, 0)

  def topNMonoid(n: Int): Monoid[PathScan] = new Monoid[PathScan] {
    def empty: PathScan = PathScan.empty

    def combine(p1: PathScan, p2: PathScan): PathScan = ???
  }

}

case class FileSize(path: Path, size: Long)

object FileSize {

  def ofFile(file: Path) = {
    FileSize(file, Files.size(file))
  }

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
