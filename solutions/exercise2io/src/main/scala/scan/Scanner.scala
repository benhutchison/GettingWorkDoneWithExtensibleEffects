package scan

import java.nio.file._

import scala.compat.java8.StreamConverters._
import scala.collection.SortedSet


import cats._
import cats.data._
import cats.implicits._


import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

import cats.effect._

import org.atnos.eff.addon.cats.effect.IOEffect._
import org.atnos.eff.syntax.addon.cats.effect._

object Scanner {

  type R = Fx1[IO]

  def main(args: Array[String]): Unit = run[R](args).unsafeRunSync

  def run[R: _io](args: Array[String]): Eff[R, Unit] = for {
    r <- scanReport(Paths.get(args(0)), 10)
  } yield println(r)

  def scanReport[R: _io](base: Path, topN: Int): Eff[R, String] = for {
    scan <- pathScan(base, topN)
  } yield ReportFormat.largeFilesReport(scan, base.toString)

  def pathScan[R: _io](path: Path, topN: Int): Eff[R, PathScan] = for {
    fp <- FilePath(path)

    scan <- fp match {

      case File(_) => for {
        fs <- FileSize.ofFile(path)
      } yield PathScan(SortedSet(fs), fs.size, 1)

      case Directory(_) => for {
        files <- ioDelay {
          val jstream = Files.list(path)
          try jstream.toScala[List]
          finally jstream.close()
        }
        subScans <- files.traverse(pathScan(_, topN))
      } yield subScans.combineAll(PathScan.topNMonoid(topN))

      case Other(_) =>
        PathScan.empty.pureEff[R]
    }
  } yield scan

}


sealed trait FilePath {
  def path: String
}
object FilePath {

  def apply[R: _io](path: Path): Eff[R, FilePath] = ioDelay(
    if (Files.isRegularFile(path))
      File(path.toString)
    else if (Files.isDirectory(path))
      Directory(path.toString)
    else
      Other(path.toString)
  )
}
case class File(path: String) extends FilePath
case class Directory(path: String) extends FilePath
case class Other(path: String) extends FilePath

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

case class FileSize(path: Path, size: Long)

object FileSize {

  def ofFile[R: _io](file: Path) = ioDelay(FileSize(file, Files.size(file)))

  implicit val ordering: Ordering[FileSize] = Ordering.by[FileSize, Long](_.size).reverse

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
