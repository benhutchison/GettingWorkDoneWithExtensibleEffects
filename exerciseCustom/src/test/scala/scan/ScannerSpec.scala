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

  type R = Fx.fx4[Task, FilesystemCmd, Reader[ScanConfig, ?], Writer[Log, ?]]

  def run[T](program: Eff[R, T]) =
    program.runReader(ScanConfig(2)).runFilesystemCmds(fs).taskAttempt.runWriter.runAsync.runSyncUnsafe(3.seconds)

  val expected = Right(new PathScan(SortedSet(FileSize(sub3, 3), FileSize(base2, 2)), 7, 4))
  val expectedLogs = Set(
    Log.info("Scan started on Directory(base)"),
    Log.debug("Scanning directory 'Directory(base)': 1 subdirectories and 2 files"),
    Log.debug("File base/1.txt Size 1 B"),
    Log.debug("File base/2.txt Size 2 B"),
    Log.debug("Scanning directory 'Directory(base/subdir)': 0 subdirectories and 2 files"),
    Log.debug("File base/subdir/1.txt Size 1 B"),
    Log.debug("File base/subdir/3.txt Size 3 B")
  )

  val (actual, logs) = run(Scanner.pathScan(base))

  "Report Format" ! {actual.mustEqual(expected)}

  "Logs messages are emitted (ignores order due to non-determinstic concurrent execution)" ! {
    logs.forall(expectedLogs.contains)
  }
}
