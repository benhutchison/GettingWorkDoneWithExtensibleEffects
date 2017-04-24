package scan

import java.io.IOException

import org.specs2._

import scala.collection.immutable.SortedSet

import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._

class ScannerSpec extends mutable.Specification {

  case class MockFilesystem(directories: Map[Directory, List[FilePath]], fileSizes: Map[File, Long]) extends Filesystem {

    def length(file: File) = fileSizes.getOrElse(file, throw new IOException())

    def listFiles(directory: Directory) = directories.getOrElse(directory, throw new IOException())
  }

  {
    val base = Directory("base")
    val base1 = File(s"${base.path}/1.txt")
    val base2 = File(s"${base.path}/2.txt")
    val subdir = Directory(s"${base.path}/subdir")
    val sub1 = File(s"${subdir.path}/1.txt")
    val sub3 = File(s"${subdir.path}/3.txt")
    val fs = MockFilesystem(
      Map(
        base -> List(base1, base2, subdir),
        subdir -> List(sub1, sub3)
      ),
      Map(base1 -> 1, base2 -> 2, sub1 -> 1, sub3 -> 3)
    )

    val expected = Right(new PathScan(SortedSet(FileSize(sub3, 3), FileSize(base2, 2)), 7, 4))
    val expectedLogs = List(
      Log.info("Scan started on Directory(base)"),
      Log.debug("Scanning directory 'Directory(base)': 1 subdirectories and 2 files"),
      Log.debug("File base/1.txt Size 1 B"),
      Log.debug("File base/2.txt Size 2 B"),
      Log.debug("Scanning directory 'Directory(base/subdir)': 0 subdirectories and 2 files"),
      Log.debug("File base/subdir/1.txt Size 1 B"),
      Log.debug("File base/subdir/3.txt Size 3 B")
    )
    val (actual, logs) = Await.result(Scanner.pathScan(base, 2, fs).runAsync, 1.seconds)

    "Report Format" ! {actual.mustEqual(expected)}
    "Logs" ! {logs.mustEqual(expectedLogs)}
  }

}
