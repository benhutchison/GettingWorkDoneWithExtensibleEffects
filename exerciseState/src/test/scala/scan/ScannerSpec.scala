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
  val linkTarget = File(s"/somewhere/else/7.txt")
  val base1 = File(s"${base.path}/1.txt")
  val baseLink = Symlink(s"${base.path}/7.txt", linkTarget)
  val subdir = Directory(s"${base.path}/subdir")
  val sub2 = File(s"${subdir.path}/2.txt")
  val subLink = Symlink(s"${subdir.path}/7.txt", linkTarget)
  val directories = Map(
    base -> List(subdir, base1, baseLink),
    subdir -> List(sub2, subLink)
  )
  val fileSizes = Map(base1 -> 1L, sub2 -> 2L, linkTarget -> 7L)
  val fs = MockFilesystem(directories, fileSizes)

  type R = Fx.fx5[Task, Reader[Filesystem, ?], Reader[ScanConfig, ?], Writer[Log, ?], State[Set[FilePath], ?]]

  def run[T](program: Eff[R, T], fs: Filesystem) =
    program.runReader(ScanConfig(2)).runReader(fs).evalStateZero[Set[FilePath]].taskAttempt.runWriter[Log].runAsync.runSyncUnsafe(3.seconds)

  val expected = Right(new PathScan(SortedSet(FileSize(linkTarget, 7), FileSize(sub2, 2)), 10, 3))

  val (actual, logs) = run(Scanner.pathScan[R](base), fs)

  "Report Format" ! {actual.mustEqual(expected)}

}
