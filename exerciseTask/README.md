# Asynchronous Tasks


A `Task[T]` represent a chunk of work that yields a value of type `T` on completion.

Tasks allow fine control over what threads run the task and with what level of concurrency. They also allow the separation
 of the definition of work (when tasks are created and chained together) from its execution (when tasks are run and any
 side-effects occur). In this way, they play a similar role to the `IO` wrappers of Haskell, Cats-Effect or Scalaz.

## The Monix Task Library

We will use [Monix 3.x Tasks](https://monix.io/docs/3x/eval/task.html).

We choose Monix over [Cats Effect IO](https://typelevel.org/cats-effect/datatypes/io.html) because Monix offers a better
default [Execution Model](https://monix.io/docs/3x/execution/scheduler.html#execution-model) for Tasks and more options
 than Cats-Effect does, and is relatively similar in other aspects.

We choose Monix tasks over Scala's default Futures because of Monix's
[Execution Model](https://monix.io/docs/3x/execution/scheduler.html#execution-model) has more control and much more performant
 defaults than Scala Futures.

## Tasks

### :mag: _Study Code_

- In the classic version, method `scanReport` returned `String`. What is its return type now? What about method `pathScan`?

### :pencil: _Write Code_

- The top level `main` still returns `Unit`. It builds a program, a chain of `Task`s, but they'll have no effect until they're run.
  Use `runSyncUnsafe(1.minute)` to run the tasks. The word Unsafe in it's name indicates that it will cause side-effects.
  The idea with Tasks is to run them at only one at the top of your top program.

- There is a compile error in `pathScan`. The `map` method no longer has the correct type, because we want to traverse a list
 of subdirectories, converting each into a `Task` yielding their scan, and then roll the traversal into a single overall Task.
 Replace `map` with `traverse`, which is "like map, but for mapping to effectful values"

- One more will appear problem. Tasks can be run asynchronously, so they need an implicit `Scheduler` available to dispatch them to.
  Add `implicit val s = Scheduler(ExecutionModel.BatchedExecution(32))` at the top of `Scanner` object. The
  `BatchedExecution(32)` means that a chain of up to 32 Tasks will be run batched together in one thread before any context switch.


### :arrow_forward: _Run Code_

Run the tests to verify your task based implementation still gives the correct output.

### :mag: _Study Code_

- Examine the tests for the Task version. Notice that the introduction of Task, which makes this version *purely functional*
hasn't made the tests any easier to write. Still lots of messy files and directories created and cleaned-up. Why is this
and what could you do about it?



