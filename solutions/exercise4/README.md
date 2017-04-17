# Exercise 4: Asynchronous Tasks & Parallelization

The scanning of a directory tree can be done in parallel by processing each subdirectory in separate tasks. Tasks represent
a chunk of work, and allow fine control over what threads run the task and with what level of concurrency.

In this exercise we will use [Monix Tasks](https://monix.io/docs/2x/eval/task.html) with Eff, and investigate running the
scanner in parallel versus serially.

Note we choose Monix tasks over Scala's default Futures in part because they let us specify whether a series of tasks run
together in the same thread, or if each task is individually executed by the thread pool. Futures always have the latter behavior.
 Refer to the docs for further details.

## Tasks

1. Study the example code, noting

   - The [effect stack `R`](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise4/src/main/scala/scan/Scanner.scala#L29)
    now includes `Task` (from `import org.atnos.eff.addon.monix._`)

   - The interpretation of the stack has changed in *two* places. Firstly, the very final step of the interpretation chain
   is now `runAsync`, which takes an Eff expression with only the Task effect remain and transforms it into a `Task`
   (ie the Eff wrapper gone). Secondly the Monix task itself is run, at "the end of the world", in the
   [main method](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise4/src/main/scala/scan/Scanner.scala#L31),
   using a monix method co-incidentally also named [`runAsync`](https://monix.io/docs/2x/eval/task.html#execution-runasync--foreach).

   - Tasks will be run in the [default thread pool provided by Monix](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise4/src/main/scala/scan/Scanner.scala#L25)
    which creates one thread per CPU (as reported to JVM by `Runtime.getRuntime().availableProcessors()`).

   - It now reports some information about how many milliseconds the scan took.


2. You will need to complete `PathScan.scan` to run subdirectory scans as Tasks, and indicate they are independent of each other.

    - First add the `_Task` Member constraint, which comes from `import org.atnos.eff.addon.monix.task._`

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


3. Run the tests to verify your task based implementation still gives the correct output.


4. Choose a directory tree cotaining over 1000 files, `run` your program, and take some timings. (See 6. below if you hit
Stack Overflows)


5. The Monix default is to run a series Task serially in the same thread to avoid thread context switches. So the above code will *not
actually be scanning directories in parallel*. To mark subtasks as eligible to run in another thread, they should be wrapped in
a `Task.fork()` call.

When the tasks are represented as Eff effects, it can be slightly tricky to fork them. The `PathScan.taskFork` method achieves
this by *interception*: it transforms an Eff program into one where every Task is `fork`ed, when the program is interpreted.

Modify the directory traversal case in `PathScan.scan`: try wrapping `PathScan.taskFork` around the invocation of `pathScan`
on each child file (ie around `taskSuspend`).

Then re-run the same scan as in step 4. Did the timing improve, worsen or remain the same when forking? If you can, examine and
contrast your CPU usage when using the forked vs non-forked versions?

In the author's tests on a quad-core Mac, the single-threaded, non-forked version consistently runs 2-3x faster.
So the naive conclusion that scanning directories in parallel would be faster doesn't seem true. The probable explanation
is that listing single directory is pretty lightweight, and the overhead of synchronizing work across lots of different
threads exceeds the cost of doing the work itself.


6. Try running your program on a very large directory tree. What happens? You should be a StackOverflow error. This is due to an
unfortunate limitation in Eff currently, where `traverseA` is not "stack safe".
