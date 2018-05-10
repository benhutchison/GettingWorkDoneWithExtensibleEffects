# Exercise 3: Error handling without exceptions

Exceptions are "considered harmful" in functional programming. The core of the objection is that they
require a special execution mode from the runtime, that has very different behaviour to nomrla execution.
But using effects, its possible to implement exception-like behavior using pure functions and with no special runtime
 support, that can readily handle almost all error scenarios.

In this version, there are two sources of errors:

- Invalid user input

- The `Filesystem` API abstracted in the reader exercise throws IOExceptions to signal errors in file system operations, such as listing
 a directory that doesnt exist.

## Tasks

### :mag: _Study Code_

- The set of Effects used at the top level of the program now includes a `Either[String, ?]` effect. The `?` here in the type represents a payload type which will be filled in when
a computation using this effect occurs (a type like this containing unfilled parameters, or holes, is called *higher-kinded*)
This effect is used to model errors resulting from validating user input.

- Exceptions thrown by existing API methods are automatically caught and stored by the Task effect that is already present in the stack.
A program that might throw an exception can be represented by the effect `Either[Throwable, ?]`. We'll deal with any
exceptions the task effect might have stored in the exercise below.

- Note how the interpretation of the Eff program now includes `runEither` to resolve the error effect. Note how `main`
has to deal with the possiblity of error, because the interpretation result becomes an `Either`.

-  Despite wrapping an `Either` around the scan result, note how the return type of `scanReport`
 remains unchanged from the previous version, as `Eff[R, PathScan]`. This is a notable feature of the Eff-style of programming;
 the Eff expression just specifies the stack type (`R`) and the payload type (`PathScan` here). To understand fully what
 effects are going on, it's necessary to look at what `Member` typeclasses are declared on the `R` type.

   Which leads to the next task, adding a Member for the error effect..

### :pencil: _Write Code_

- The `scanReport` method is doing validation that will raise errors, so we need to declare that an `Either[String, ?]` effect
must be present. Do this by adding `_err` to the context bounds on the effect stack `R`. Where is `_err` defined?

 - In `scanReport`, fill in the `???` by validating the `topN` Int value is >= 0. The eff library defines a combinator for raising an error
called `left`, it is already available via `import org.atnos.eff.all._`. An invalid int value should result in a message
like "Invalid number of files -1". A valid value will need to be lifted into an Eff expression using `.pureEff[R]`

- Exceptions thrown from the Filesystem and trapped by the Task effect will be rethrown when we call `runSyncUnsafe`.
Lets instead convert them to an `Either[String, ?]` and combine them with the validation Either.

   Use the `attempt` combinator on `Task`, adding it after `runAsync`. This materializes any trapped exceptions and returns
   a `Task[Either[Throwable, T]]` that won't throw exceptions when run.

   But there's still a problem. We end up with two different types of errors in our result payload, `Throwable` and `String`.
   Add `.leftMap(_.toString).flatten` to the end of the interpretation to convert the Throwables to Strings and unnest
   the `Either`s.

### :mag: _Study Code_

- Whats going on in `pathScan` with this code:
```
 scan <- pathScan[Fx.prepend[Reader[ScanConfig, ?], R]](fs.filePath(base)).
      runReader[ScanConfig](ScanConfig(topNValid))
```
This is an example of extending the effect stack `R` (with `Fx.prepend[Reader[ScanConfig, ?], R]`) in a part of the program.
We then interpret the effect out of the stack part-way through the program, rather than at the end, with
`runReader[ScanConfig](ScanConfig(topNValid))`.

### :pencil: _Write Code_

Examine the test code in `ScannerSpec` and note the new test for a Filesystem exception "Error from Filesystem".

Work out what value is expected when invoked on the mock filesystem provided, and replace the `???` to get the test working.

### :arrow_forward: _Run Code_

Run both tests to verify both happy and sad paths.
