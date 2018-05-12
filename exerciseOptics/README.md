# Using Optics with Eff

In this exercise we'll look at how Functional Optics (aka "Lenses") provided by the Monocle library can be used with Eff.

We have seen several examples of effects which have a second type parameter in addition to the payload:

- `Either[E, ?]` has the type of the error `E`
- `Reader[A, ?]` has the type of value to be provided/injected `A`
- `State[S, ?]` has the type of the state `S`

We can think of these effects as being families of related effects, sharing the same basic mechanism but with a particular
type focus. But from Eff's perspective, `Reader[A, ?]` and `Reader[B, ?]` are completely different effects.

It's often convenient to be able to combine effects from the same family, but with different foci, together. In this example
we'll look at how lenses can let us do that.


### :pencil: _Write Code_

- In `pathScan` we currently use the `traverse` combinator to walk through the subdirectories and recursively invoke
`pathScan` on each of them. Traverse implies that we want to process them strictly in order, but there is a variant
`traverseA` (short for "traverse applicative") which says we can visit them in any order, completing when they have all
been processed. Replace `traverse` with `traverseA` and the tasks can execute concurrently. Nothing more needed!

### :arrow_forward: _Run Code_

Run the tests to verify your task based implementation still gives the correct output.

Run the scanner on a large directory tree. Do it several times as the results will likely include noise.
Do you see a speed-up from concurrent scanning?

You may see no improvement, or only a small improvement. This may be because your hard drive or SSD has limited capability
to serve requests in parallel.


### :arrow_forward: Run Code_

By default Monix batches the execution of a series of Tasks serially in the same thread to avoid thread context switches.
The `BatchedExecution(32)` configuration in the Scanner specifies that 32 tasks should be executed by a thread before
releasing control and returning to the configured Monix `Scheduler`.

When tasks are structurally independent of each other, as is expressed by using `traverseA`, the batch size affects the
degree of concurrency that will be enabled when the program executes. Too much, and a threads can context switch wastefully.
(This is what happens with Scala `Future` and is the reason why Monix tasks typically run faster). Too little, and the available
parallelism in the underlying work may not be achieved.

Monix defaults to a batch size 1024. For tasks which do IO such as the Scanner, this may be too high. The value 32 was
derived experimentally running the program against large directory scans on a Macbook SSD drive.

- Run the Scanner on a directory tree with 100s of files, big enough that it takes approx 10secs to complete.
Try varying the batch size parameter, by doubling or halving it progressively.
Take multiple timings at each batch size. Is 32 the optimal value on your hardware or something else?

- Also, if you have a multicore machine, observe the CPU usage as you change the batch size. You may see it rise with
a smaller batch size, even if overall performance worsens. This is because its doing more work in parallel but wasting
effort on switching work between threads.
