# Exercise 1: File Scanner without Eff

This implementation uses regular Scala features to build a large file scanner:

- File system operations are done directly inline, primarily using the `java.nio.file.Files` API provided in Java 8. This
is preferable to the operations offered through the older `java.io.File` API because error conditions are signalled by an
exception rather than simply returning an uninformative `null`.

- Error handling is by throwing exceptions which will bubble to the top.

## Tasks

1. Study the algorithm used by the scanner. The key data structure is a PathScan, which contains a sorted list of the
largest N files and their sizes in bytes, plus a count of total files scanned and total bytes across all files.

    Note how the scanner must combine `PathScan`s of differing subdirectories together to yield
a single `PathScan` that summarizes both the *top N* files and *total* files visited. This combine operation has a
[Monoid](http://typelevel.org/cats/typeclasses/monoid.html) structure.

    Complete the [Monoid instance for PathScan](src/main/scala/scan/Scanner.scala#L42). Task 2 will test its correctness.

2.  Work out in your head the the [ScannerSpec expected value](src/test/scala/scan/ScannerSpec.scala) for the provided test data.
Get the test running and use it to test your Monoid implementation.

3. Run the scanner on a directory of your computer. Does the result seem correct?

4. Return to the ScannerSpec. Note how much of the test is involved with creating- and cleaning up- actual files. It would be nice
is we could separate testing the algorithm from the filesystem. How should this be done?
