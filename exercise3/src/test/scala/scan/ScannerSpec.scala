package scan

import java.io.{FileNotFoundException, IOException}

import org.specs2._

import scala.collection.immutable.SortedSet

class ScannerSpec extends mutable.Specification {

  case class MockFilesystem(directories: Map[Directory, List[FilePath]], fileSizes: Map[File, Long]) extends Filesystem {

    def length(file: File) = fileSizes.getOrElse(file, throw new IOException())

    def listFiles(directory: Directory) =
      directories.getOrElse(directory, throw new FileNotFoundException(directory.path))
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

    val actual = Scanner.pathScan(base, 2, fs)
    val expected = Right(new PathScan(SortedSet(FileSize(sub3, 3), FileSize(base2, 2)), 7, 4))

    actual.mustEqual(expected)
  }

  "Error handling" ! {
    val base = Directory("base")
    val fs = MockFilesystem(Map.empty, Map.empty)

    val actual = Scanner.pathScan(base, 2, fs)
    val expected = Left(new FileNotFoundException("base"))

    //Cant directly compare 2 different FileNotFoundException instances for equality, so convert to Strings first
    actual.toString.mustEqual(expected.toString)
  }

}
