# Exercise 5: Logging with the Writer effect

The `Writer[L, ?]` effect lets the computation emit additional values of type `L` as a side-effect of computation. It most commonly
used to functional logging.

Exercise 5 is to study a use-case of functional logging, and how "logs-as-values" lets us easily
write unit tests around the log output.


## Tasks

1. Study the example code, noting

   - The [effect stack `R`] now includes `Writer[Log]`, where `Log` is a sealed hierarchy of Log events at different levels.

   - In `PathScan.scan`, the `tell` operator is used to emit `Log` values. Because `tell` just emits a log, it has type
   `Eff[R, Unit]`. So an underscore is used on the lefthand-side of the for (eg `_ <- tell(x)`).

   - In `Scanner.pathScan`, the interpretation of the Eff program now includes a `runWriter` step. The final result
   of the program is now a *pair*, consisting of the payload and a `List[Log]` being a list of logs that were emitted.

   - The logs are passed up to the top level where they are `println`ed out.


2. Run the tests. They now verify not just the program output but the logs. They should fail because they expect log output
for each file visited by the scan.

    - Make the test pass by adding the appropriate `tell` statement to the `File` case in `PathScan.scan`.


# Exercise 5a: Flexible Interpretation

This is a more open-ended extension activity. No solution is provided.

We'll make some changes to our logging strategy and observe that the impact bleeds across a large surface
area of our program. We'll use this a vehicle to examine a different approach to interpreting an Eff program, and show how
often leaving interpretation to the last possible moment results in more flexible and elegant code.

## Tasks

1. One downside of the logging strategy used above is that all the logs are accumulated in memory until the end of the
computation. A big computation could emit alot of logs, and we might not wish to cache them to the end.

Eff gives us options. One provided by `runWriterUnsafe` is to send a side-effecting handler function (eg `println`)
into the interpreter, that logs each event as its emitted, rather than returning an accumulation.

Try changing `Scanner.pathScan` to use `runWriterUnsafe[Log](println)`.

2. You'll observe that we are no longer returning our logs, so there are now compiler errors in `Scanner.scanReport` and
`Scanner.main`. Fix these so that the program compiles and runs again.

3. Quite alot of code had to be touched just to change the logging strategy! Try to re-organize the code
 so that changes to logging are more contained.

In all the examples so far, we've done processing over the result of Eff interpretation. This had some benefits, like
allowing the test code to ignore Eff & interpretation. However another approach is to try to do everything inside Eff,
and interpret the Eff program at the last possible moment (ie in `main`). This implies that the test code has its own
interpret step as well.




