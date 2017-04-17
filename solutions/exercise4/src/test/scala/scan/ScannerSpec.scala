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

  "Report Format" ! {
    val base = Directory("base")
    val base1 = File(s"${base.path}/1.txt")
    val base2 = File(s"${base.path}/2.txt")
    val subdir = Directory(s"${base.path}/subdir")
    val sub1 = File(s"${subdir.path}/1.txt")
    val sub3 = File(s"${subdir.path}/3.txt")
    val fs = MockFilesystem(
      Map(
        base -> List(subdir, base1, base2),
        subdir -> List(sub1, sub3)
      ),
      Map(base1 -> 1, base2 -> 2, sub1 -> 1, sub3 -> 3)
    )

    val actual = Await.result(Scanner.pathScan(base, 2, fs).runAsync, 1.seconds)
    val expected = Right(new PathScan(SortedSet(FileSize(sub3, 3), FileSize(base2, 2)), 7, 4))

    actual.mustEqual(expected)
  }

}
