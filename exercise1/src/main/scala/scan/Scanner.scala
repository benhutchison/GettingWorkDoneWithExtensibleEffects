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
      val files = {
        val jstream = Files.list(dir)
        try jstream.toScala[List]
        finally jstream.close()
      }
      files.map(pathScan(_, topN)).combineAll(PathScan.topNMonoid(topN))
    case _ =>
      PathScan.empty
  }

}

//Represents the results of recursively scanning some directory tree
case class PathScan(
  //Sorted set of the largest files found in the tree
  largestFiles: SortedSet[FileSize],
  //total bytes all files scanned in the tree
  totalSize: Long,
  //total number of files encountered in the tree
  totalCount: Long
)

object PathScan {

  def empty = PathScan(SortedSet.empty, 0, 0)

  //topNMonoid defines a Monoid operator that can combine different PathScans together to produce a summary PathScan
  //It accepts a parameter `n` specifying how many of the largest files to keep track off
  def topNMonoid(n: Int): Monoid[PathScan] = new Monoid[PathScan] {
    def empty: PathScan = PathScan.empty

    //The combine operation of a Monoid should yield a combined, summary PathScan based on the two inputs
    //Only the largest `n` files should be included in the combined scan
    def combine(p1: PathScan, p2: PathScan): PathScan = ???
  }

}

case class FileSize(path: Path, size: Long)

object FileSize {

  def ofFile(file: Path) = {
    FileSize(file, Files.size(file))
  }

  //The implicit declares the default ordering of FileSize objects is by file size (in bytes)
  implicit val ordering: Ordering[FileSize] = Ordering.by[FileSize, Long](_.size)

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
