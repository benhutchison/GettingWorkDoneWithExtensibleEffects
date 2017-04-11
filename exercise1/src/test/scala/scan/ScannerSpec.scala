package scan

import java.io.PrintWriter
import java.nio.file._

import org.specs2._

import scala.collection.immutable.SortedSet

class ScannerSpec extends mutable.Specification {

  "Report Format" ! {
    val base = deletedOnExit(Files.createTempDirectory("exercise1"))
    val base1 = deletedOnExit(fillFile(base, 1))
    val base2 = deletedOnExit(fillFile(base, 2))
    val subdir = deletedOnExit(Files.createTempDirectory(base, "subdir"))
    val sub1 = deletedOnExit(fillFile(subdir, 1))
    val sub3 = deletedOnExit(fillFile(subdir, 3))

    val actual = Scanner.pathScan(base, 2)
    val expected = new PathScan(SortedSet(FileSize(sub3, 3), FileSize(base2, 2)), 7, 4)

    actual.mustEqual(expected)
  }

  def fillFile(dir: Path, size: Int) = {
    val path = dir.resolve(s"$size.txt")
    val w = new PrintWriter(path.toFile)
    try w.write("a" * size)
    finally w.close
    path
  }

  def deletedOnExit(p: Path) = {
    p.toFile.deleteOnExit()
    p
  }

}
