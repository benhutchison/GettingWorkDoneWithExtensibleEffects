package scan

import java.io._
import java.io._
import java.nio.file._

import cats._
import cats.data._
import cats.implicits._

import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

import org.atnos.eff.addon.monix._
import org.atnos.eff.addon.monix.task._
import org.atnos.eff.syntax.addon.monix.task._

import org.specs2._

import scala.collection.immutable.SortedSet

import scala.concurrent.duration._

import monix.execution.Scheduler.Implicits.global

class ScannerSpec extends mutable.Specification {

  case class MockFilesystem(directories: Map[Directory, List[FilePath]], fileSizes: Map[File, Long]) extends Filesystem {

    def length(file: File) = fileSizes.getOrElse(file, throw new IOException())

    def listFiles(directory: Directory) = directories.getOrElse(directory, throw new IOException())

    def filePath(path: String): FilePath =
      if (directories.keySet.contains(Directory(path)))
        Directory(path)
      else if (fileSizes.keySet.contains(File(path)))
        File(path)
      else
        throw new FileNotFoundException(path)
  }

  "file scan" ! {
    val base = Directory("base")
    val base1 = File(s"${base.path}/1.txt")
    val base2 = File(s"${base.path}/2.txt")
    val subdir = Directory(s"${base.path}/subdir")
    val sub1 = File(s"${subdir.path}/1.txt")
    val sub3 = File(s"${subdir.path}/3.txt")
    val fs: Filesystem = MockFilesystem(
      Map(
        base -> List(subdir, base1, base2),
        subdir -> List(sub1, sub3)
      ),
      Map(base1 -> 1, base2 -> 2, sub1 -> 1, sub3 -> 3)
    )

    val program = Scanner.pathScan[Scanner.R](base)
    val actual = program.runReader(ScanConfig(2)).runReader(fs).runAsync.runSyncUnsafe(3.seconds)
    val expected = new PathScan(SortedSet(FileSize(sub3, 3), FileSize(base2, 2)), 7, 4)

    actual.mustEqual(expected)
  }
}
