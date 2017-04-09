package service

import java.io.File

import cats._
import cats.implicits._


import scala.collection.SortedSet

object Service {

  def main(args: Array[String]): Unit = {
    val rootDir = new File(args(0))
    val scan = pathScan(rootDir, 10)

    println(largeFilesReport(scan, rootDir))
  }

  def largeFilesReport(scan: PathScan, rootDir: File): String = {
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

  def pathScan(path: File, topN: Int): PathScan = path match {
    case file if path.isFile() && path.exists() =>
      val fs = FileSize.ofFile(path)
      PathScan(SortedSet(fs), fs.size, 1)
    case dir if path.isDirectory() =>
      val optFiles = Option(dir.listFiles())
      optFiles.fold(PathScan.empty)(
        _.toList.foldMap(pathScan(_, topN))(PathScan.topNMonoid(topN)))
    case _ =>
      PathScan.empty
  }

}

case class PathScan(largestFiles: SortedSet[FileSize], totalSize: Long, totalCount: Long)

object PathScan {

  def empty = PathScan(SortedSet.empty, 0, 0)

  def topNMonoid(n: Int): Monoid[PathScan] = new Monoid[PathScan] {
    def empty: PathScan = PathScan.empty

    def combine(p1: PathScan, p2: PathScan): PathScan = PathScan(
      p1.largestFiles.union(p2.largestFiles).take(n),
      p1.totalSize + p2.totalSize,
      p1.totalCount + p2.totalCount
    )
  }

}

case class FileSize(path: File, size: Long)

object FileSize {

  def ofFile(file: File) = {
    FileSize(file, file.length())
  }

  implicit val ordering: Ordering[FileSize] = Ordering.by[FileSize, Long  ](_.size).reverse

}
