package scan

import java.io.FileNotFoundException
import java.io.IOException
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
import monix.eval._
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

  val base = Directory("base")
  val base1 = File(s"${base.path}/1.txt")
  val base2 = File(s"${base.path}/2.txt")
  val subdir = Directory(s"${base.path}/subdir")
  val sub1 = File(s"${subdir.path}/1.txt")
  val sub3 = File(s"${subdir.path}/3.txt")
  val directories = Map(
    base -> List(subdir, base1, base2),
    subdir -> List(sub1, sub3)
  )
  val fileSizes = Map(base1 -> 1L, base2 -> 2L, sub1 -> 1L, sub3 -> 3L)
  val fs = MockFilesystem(directories, fileSizes)

  type R = Fx.fx3[Task, Reader[Filesystem, ?], Reader[ScanConfig, ?]]

  def run[T](program: Eff[R, T], fs: Filesystem) =
    program.runReader(ScanConfig(2)).runReader(fs).runAsync.attempt.runSyncUnsafe(3.seconds)

  "file scan" ! {
    val actual = run(Scanner.pathScan(base), fs)
    val expected = Right(new PathScan(SortedSet(FileSize(sub3, 3), FileSize(base2, 2)), 7, 4))

    actual.mustEqual(expected)
  }

  "Error from Filesystem" ! {
    val emptyFs: Filesystem = MockFilesystem(directories, Map.empty)

    val actual = runE(Scanner.scanReport(Array("base", "10")), emptyFs)
    val expected =  Left(new IOException().toString)

    actual.mustEqual(expected)
  }

  type E = Fx.fx3[Task, Reader[Filesystem, ?], Either[String, ?]]
  def runE[T](program: Eff[E, T], fs: Filesystem) =
    //there are two nested Either in the stack, one from Exceptions and one from errors raised by the program
    //we convert to a common error type String then flatten
    program.runReader(fs).runEither.runAsync.attempt.runSyncUnsafe(3.seconds).leftMap(_.toString).flatten

  "Error - Report with non-numeric input" ! {
    val actual = runE(Scanner.scanReport(Array("base", "not a number")), fs)
    val expected = Left("Number of files must be numeric: not a number")

    actual.mustEqual(expected)
  }

  "Error - Report with non-positive input" ! {
    val actual = runE(Scanner.scanReport(Array("base", "-1")), fs)
    val expected = Left("Invalid number of files -1")

    actual.mustEqual(expected)
  }
}
