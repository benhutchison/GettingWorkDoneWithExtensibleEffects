# Exercise 3: Error handling without exceptions

The `Filesystem` API abstracted in exercise 2 throws IOExceptions to signal errors in file system operations, such as listing
 a directory that doesnt exist. Exceptions are "considered harmful" in functional programming.

The next version of the program will use a `Either[Throwable, PathScan]` type to model a computation yielding an `PathScan`
 that may instead fail with a `Throwable`. Both the success and error paths are consistently modelled simply using functions that
 return values, maintaining referential transparency and without needing differing language semantics for exceptional flows.

## Tasks

1. Examine the changes in this version. Identify:

- The [set of Effects used at the top level of the program](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise3/src/main/scala/scan/Scanner.scala#L21)
now includes a 3rd effect `Either[Throwable, ?]`. The `?` here in the type represents a payload type which will be filled in when
a computation using this effect occurs (a type like this containing unfilled parameters, or holes, is called *higher-kinded*)

- Note how the interpretation of the Eff program now includes `runEither` to resolve the error effect. Note how the
[calling code](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise3/src/main/scala/scan/Scanner.scala#L25)
is forced to deal with the possiblity of error, because the interpretation result becomes an `Either`.

-  Despite wrapping an `Either` around the scan result, note how the return type of [PathScan.scan](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise3/src/main/scala/scan/Scanner.scala#L86)
 remains unchanged from the previous version, as `Eff[R, PathScan]`. This is a notable feature of the Eff-style of programming;
 the Eff expression just specifies the stack type (`R`) and the payload type (`PathScan` here). To understand fully what
 effects are going on, it's necessary to look at what `Member` typeclasses are declared on the `R` type.

   Which leads to the next task, adding a Member for the error effect..


2. The eff library [pre-defines a member typeclass](https://github.com/atnos-org/eff/blob/81fd2affeab65e9621cb4a6cba35d0539d201954/shared/src/main/scala/org/atnos/eff/EitherEffect.scala#L23)
for the `Either[Throwable, ?]` effect, and it is already available via `import org.atnos.eff.all._`.

    To ensure that exceptions thrown by calls to the `Filesystem` are caught and transformed into `Either` values, two changes are
  required to `PathScan.scan` and `FileSize.ofSize`, the code sites where the filesystem is accessed.

  - Firstly, add the member constraint `_throwableEither` on the effect type `R` declared in each method. This states that
  this method may use the `Either` effect.

  - Secondly, wrap the [`catchNonFatalThrowable`](https://github.com/atnos-org/eff/blob/81fd2affeab65e9621cb4a6cba35d0539d201954/shared/src/main/scala/org/atnos/eff/EitherEffect.scala#L46)
  method around filesystem calls, and extract them into their own line in the for expression. This combinator yields whatever
  it wraps as a payload, but thrown exceptions are caught and converted to a `Left[Throwable]`.


3. Examine the test code in `ScannerSpec` and note the new test for the error case. Work out what value you would expect
the scan to yield, when invoked on the mock filesystem provided, and replace the `???` to get the test working.


4. Run the tests to verify your changes are working correctly.
