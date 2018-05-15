# Stafeful computation with the State effect

The `State[S, ?]` models a stateful computation that receives a current state `S`, and emits a new state `S` along with
the payload.

We will use `State` to try to account for the effect of symlinks in our file traversal. If we traverse a directory tree
that contains multiple symlinks to the same target file, we won't count the size of the target file more than once.

To do this we'll need to keep track if what files we've seen before. This is what requires the use of a State effect.
In this case, the state we'll be tracking (ie type `S`) will be `Set[FilePath]`


## Tasks

### :mag: _Study Code_

   - Note that `FilePath` now includes a new case `Symlink(path: String, linkTo: FilePath)` and the `DefaultFilesystem` uses the
   File API to distinguish symlinks. Previously they were automatically followed.


### :pencil: _Write Code_

- Define an alias `_sym[R]` in `EffTypes` to indicate that `State[Set[FilePath], ?]` is a member of effect stack `R`

- Add the member constraint to `pathScan` and `scanReport`

- We need to interpret the state effect in `main`. We'll use the
[`evalStateZero[Set[FilePath]]`](https://github.com/atnos-org/eff/blob/4d289be/shared/src/main/scala/org/atnos/eff/syntax/state.scala#L28)
 combinator. Eval here means
that the final state isn't returned, just the computed payload. The zero suffix indicates that the zero value of a
`Monoid[Set[FilePath]]` in scope should be used as the initial state. That's coming in from `imports cats.implicits._`.
The zero value of a set is `Set.empty`.

- In `pathScan`, you'll should add a case to handle `Symlink`. To read/write the current state use the `get`/`put` combinators,
and/or `modify(f: S => S)` to run a state modification function. Your logic should check if the target of a symlink has been
visited; if it has, then return an empty pathscan, while if it hasn't, add it to the visited set and invoke `pathScan` on the target.

   This is a tricky task. Remember to look at the [solution](../solutionExerciseState/src/main/scala/scan/Scanner.scala)
   for guidance if you get stuck.

### :arrow_forward: _Run Code_

- Run the test to check that symlinks are being handled.








