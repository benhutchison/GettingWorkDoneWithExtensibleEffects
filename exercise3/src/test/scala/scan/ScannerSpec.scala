package scan

import java.io.{FileNotFoundException, IOException}

import org.specs2._

class ScannerSpec extends mutable.Specification {

  case class MockFilesystem(directories: Map[Directory, List[FilePath]], fileSizes: Map[File, Long]) extends Filesystem {

    def length(file: File) = fileSizes.getOrElse(file, throw new IOException())

    def listFiles(directory: Directory) =
      directories.getOrElse(directory, throw new FileNotFoundException(directory.path))
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
