# Exercise 4: Parallel Scanning with Asynchronous Tasks

The scanning of a directory tree can be done in parallel by processing each subdirectory in separate tasks. Tasks represent
a chunk of work, and allow fine control over what threads run the task and with what level of concurrency.

In this version, we use Eff's integration with [Monix Tasks](https://monix.io/docs/2x/eval/task.html) in preference to
Scala's default Futures. Monix tasks are generally better designed and more performant than Futures, but the details of why
are outside this workshop, refer to the docs for details.

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

2. You will need to [complete PathScan.scan](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise4/src/main/scala/scan/Scanner.scala#L101)
to run subdirectory scans as Tasks, and indicate they can be concurrently.

    - First add the `_task` Member constraint, which comes from `import org.atnos.eff.addon.monix.task._`

    - Each file or subdirectory listed in the directory can be scanned as a separate Task. The file tasks will quickly return,
    while the subdirectory tasks may themselves spawn more subtasks.

       To build an Eff expression yielding an `A` inside a task, use the
    [`taskSuspend`](https://github.com/atnos-org/eff/blob/81fd2affeab65e9621cb4a6cba35d0539d201954/monix/shared/src/main/scala/org/atnos/eff/addon/monix/TaskEffect.scala#L26)
    combinator. This combinator accepts a parameter of type `Task[Eff[R, A]]`, so the construction of the Eff subexpression
    is deferred until needed. [`Task.eval`](https://monix.io/docs/2x/eval/task.html#taskeval-delay)
    creates a Task that lazily evaluates its contents.

    - But this leaves the problem of how to convert a `List[File]` into a collection of `Task`s that can be run concurrently.
    Eff provides the *applicative-traversal* [`Eff.traverseA`](https://github.com/atnos-org/eff/blob/81fd2affeab65e9621cb4a6cba35d0539d201954/shared/src/main/scala/org/atnos/eff/Eff.scala#L276)
    operator for this: feed it a list of files, and a function that converts each file
    into a `Task`, and it will build an Eff computation that marks the subtasks as independent of each other,
     and thus parallelizable.

       Recall that actual concurrent task execution happens
       [when the task is run](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/blob/master/exercise4/src/main/scala/scan/Scanner.scala#L31),
       *not* when the Eff expression is built.

    - If you correctly apply the above three operators (`Eff.traverseA`, `taskSuspend` and `Task.eval`), you should end up with
       a computation that yields `List[PathScan]`. The final step is to run the `PathScan` monoid instance over the list
       to reduce it to one summary scan; `combineAll` does this.

3. Run the tests to verify your task based implementation still gives the correct output. We'll verify that it runs concurrently
in exercise 5.