# Exercise 4: Asynchronous Tasks & Parallelization

The scanning of a directory tree can be done in parallel by processing each subdirectory in separate tasks. Tasks represent
a chunk of work, and allow fine control over what threads run the task and with what level of concurrency.

## The Monix Task Library

In this exercise we will use [Monix Tasks](https://monix.io/docs/2x/eval/task.html) with Eff, and investigate running the
scanner in parallel versus serially.

Note we choose Monix tasks over Scala's default Futures in part because they let us specify whether a series of tasks run
together in the same thread, or if each task is individually executed by the thread pool. Futures always have the latter behavior.
 Refer to the docs for further details.

## Tasks

### :mag: _Study Code_

   - The [effect stack `R`](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise4/src/main/scala/scan/Scanner.scala#L30)
    now includes `Task` (from `import org.atnos.eff.addon.monix._`)

   - The interpretation of the stack has changed in *two* places. Firstly, the very final step of the interpretation chain
   is now `runAsync`, which takes an Eff expression with only the Task effect remain and transforms it into a `Task`
   (ie the Eff wrapper gone). Secondly the Monix task itself is run, at "the end of the world", in the
   [main method](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise4/src/main/scala/scan/Scanner.scala#L31),
   using a monix method co-incidentally also named [`runAsync`](https://monix.io/docs/2x/eval/task.html#execution-runasync--foreach).

   - Tasks will be run in the `Scheduler` placed in *implicit scope*. By default it
    creates one thread per CPU (as reported to JVM by `Runtime.getRuntime().availableProcessors()`). `The Scheduler` parameter
    `BatchedExecution(32)` tells monix to chunk together 32 Tasks in a row and run them on the same thread.
    It's discussed in more detail in the last task below.

   - The scanner now reports some information about how many milliseconds the scan took.


### :pencil: _Write Code_

You will need to complete `PathScan.scan` to run subdirectory scans as Tasks, and indicate they are independent of each other.

- First add the `_task` Member constraint, which comes from `import org.atnos.eff.addon.monix.task._`

- Each file or subdirectory listed in the directory can be scanned as a separate Task. The file tasks will quickly return,
while the subdirectory tasks may themselves spawn more subtasks.

   To build an Eff expression yielding an `A` inside a task, use the
[`taskSuspend`](https://github.com/atnos-org/eff/blob/81fd2affeab65e9621cb4a6cba35d0539d201954/monix/shared/src/main/scala/org/atnos/eff/addon/monix/TaskEffect.scala#L26)
combinator. This combinator accepts a parameter of type `Task[Eff[R, A]]`, so the construction of the Eff subexpression
is deferred until needed. [`Task.eval`](https://monix.io/docs/2x/eval/task.html#taskeval-delay)
creates a Task that lazily evaluates its contents.

- But this leaves the problem of how to convert a `List[File]` into a collection of `Task`s that can be run independently
(and potentially concurrent with each other).
Eff provides the *applicative-traversal* [`Eff.traverseA`](https://github.com/atnos-org/eff/blob/81fd2affeab65e9621cb4a6cba35d0539d201954/shared/src/main/scala/org/atnos/eff/Eff.scala#L276)
operator for this: feed it a list of files, and a function that converts each file
into a `Task`, and it will build an Eff computation that marks the subtasks as independent of each other.

   Recall that actual task execution happens
   [when the task is run](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise4/src/main/scala/scan/Scanner.scala#L31),
   *not* when the Eff expression is built.

- If you correctly apply the above three operators (`Eff.traverseA`, `taskSuspend` and `Task.eval`), you should end up with
   a computation that yields `List[PathScan]`. The final step is to run the `PathScan` monoid instance over the list
   to reduce it to one summary scan; `combineAll` does this.

### :arrow_forward: _Run Code_

Run the tests to verify your task based implementation still gives the correct output.


### :arrow_forward: :question: _Optional Run Code_

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

Remember that if you your the monadic `traverse`, rather than the applicative `traverseA`, the tasks will run serially
(without parallelism) regardless of the batch size. This is because monadic traversal defines a sequential structure where each task
logically depends upon the one before it.
