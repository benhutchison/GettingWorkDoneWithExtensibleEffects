# Logging with the Writer effect

The `Writer[L, ?]` effect lets the computation emit additional values of type `L` as a side-effect of computation. It is commonly
used to functional logging, but you could also view the log value as an appendix or supplimentary information about the
computation.

Exercise 5 is to study a use-case of functional logging, and how "logs-as-values" lets us easily
write unit tests around the log output.


## Tasks

### :mag: _Study Code_

   - The effect stack `R`  includes `Writer[Log]`, where `Log` is a sealed hierarchy of Log events at different levels.

   - In `PathScan.scan`, the `tell` operator is used to emit `Log` values. Because `tell` just emits a log, it has type
   `Eff[R, Unit]`. So an underscore is used on the lefthand-side of the for (eg `_ <- tell(x)`).

   - In `main`, the interpretation of the Eff program now includes a `runWriterUnsafe` step. This is obe of several
   approaches to logging offerred by Eff, where we send a side-effecting handler function (eg `println`)
   into the interpreter, that logs each event as its emitted, rather than returning an accumulation. Its not pure, but it
   has the advantage of not accumulating data in memory during execution.

   - This example also measures the time the scan took. Note how the start and end times in `scanReport` must be wrapped in
   `taskDelay` to ensure the clock times are taken when the program *runs*, and not when its is *created*.

### :arrow_forward: _Run Code_

Run the tests. They now verify not just the program output but the logs. They should fail because they expect log output
for each file visited by the scan.

### :pencil: _Write Code_

Make the test pass by adding the appropriate `tell` statement to the `File` case in `pathScan`.

### :arrow_forward: _Run Code_

`run` the scanner on a real directory tree and check the logging works as expected.

### :pencil: _Write Code_

Try changing the interpretation in main to use the plain `runWriter`.

What does the program return now? How can you print the logs?






