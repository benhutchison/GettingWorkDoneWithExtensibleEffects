# Custom Effects

In previous examples, we've been using `Task` to wrap code that is side-effecting, deferring its effects until the the
Eff program is interpreted, following the style of Haskell's `IO`.

This style, whereby all IO is wrapped in a single catch all type, is coming under increasing challenge. It ought to be
possible to be more descriptive about what type of external effects a program has, rather than simply acknowledging that
they exist.

In this example, we'll look at how custom effects can be used for this goal. We'll take the filesystem related operations
that the program uses and factor them into a filesystem effect. We'll leave the interactions with the console (via `println`)
and the clock (via `System.currentTimeMillis`) wrapped in Task, showing that custom IO effects can be introduced gradually
into a program.


### :mag: _Study Code_

- The sealed trait `FilesystemCmd[A]` defines the operations supported by the custom effect. There are 3 subclasses,
one for each operation. The generic type `A` represents the value returned when this operation is run.

- The `FilesystemCmd` companion object also defines three combinators that introduce filesystem effects into an Eff
program. Notice that the all work similarly, creating an instance of a FilesystemCmd, and then calling `Eff.send`
to add the instance into the Eff stack of effects to be resolved at interpret-time.

- As well as introducing effects, we need a way to resolve them. The abstract `Filesystem` class includes an interpreter
that resolves filesystem effects out of the stack. This has been left incomplete as an exercise for you.

- Note the presence of a `_filesystem` member constraint in the main `pathScan` method.

### :pencil: _Write Code_

- In `Filesystem`, complete the `match` clause inside `runFilesystemCmds`. You'll need to define logic for interpreting
each type of `FilesystemCmd`.


### :arrow_forward: _Run Code_

Run tests to verify your implementation works correctly.

### :mag: _Study Code_

- Examine the unit tests. How does the use of a custom test change the testing approach?

- What other effect is *not* needed in this version as a result?

