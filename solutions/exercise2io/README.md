# Exercise 2: Separating Declaration and Execution with the IO effect

In the first step, we will transform our scanner into a purely functional program by introducing `IO` as our first Eff effect.
IO "suspends" effectful actions like reading the filesystem, so that they aren't run when they're declared - while letting
us talk about and depend upon the results of such actions during program construction.

We're going to use the IO effect from [cats.effect](https://typelevel.org/cats-effect/). While it can be used standalone,
we'll bundle it inside an Eff container to make it easy to add additional effects in later exercises.

## Tasks

### :mag: _Study Code_  Declaring Effects

Examine the changes to `PathScan.scan` in this version and identify:

-   The return type is now an *Eff expression* `Eff[R, PathScan]`. This is read as *a program that when run has effects described
by the effect set `R` and yields a `PathScan` result*.

-   The effect set `R` passed a type parameter.

-   The *member typeclasses* `_filesystem` and `_config` that denote the effects that `R` must include for this function to
operate (`R` can also include other effects not used in this function).

-   The `for {..} yield` expression in the function body. Eff programs are built by flatMapping over a sequence of sub-steps.
Note the `ask` step that yields `fs` (the current `Filesystem`). Where does the `ask` method come from?

### :mag: _Study Code_  Interpreting Effects

Examine `Scanner.pathScan`. Note how we build the Eff program first, then *interpret* (run) it. To run the program, both
`Reader` effects need their dependency provided. Once these effects are resolved, the final call to `run` completes the
program and yields the final result.

- What happens if you re-order the two calls to `runReader`?
- What happens if you remove one call to `runReader`?

### :pencil: _Write Code_

Extend the use of the Reader effect to `PathScan.takeTopN` and `FileSize.ofFile`. Both of these methods should be converted
to:

- Accept a type parameter `R` and one of the member typeclasses (`_filesystem` and `_config`) denoting the dependency they
need.

- Return their result in an Eff expression

- Use a for-expression internally to `ask` for their dependency, and then `yield` their result.

    If you've made the changes correctly, there shouldn't be any manual passing of the `Filesystem` or `ScanaConfig` parameters.


### :arrow_forward: _Run Code_

Run the test to verify your changes are working correctly

### :mag: _Study Code_ Easy testing

Note how the [ScannerSpec](src/test/scala/scan/ScannerSpec.scala) tests most of the program's logic using Plain Old Scala Objects
and without doing IO

### :mag: :question: _Optional Study_

Examine the `DefaultFilesystem.listFiles` method and note the try/finally construct. The reason for this is
that `listFiles` returns a `Stream` of the directory contents, which holds open a file handle until the stream is cleaned up.

