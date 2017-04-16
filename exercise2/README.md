# Exercise 2: File Scanner with Reader configuration injection

Hopefully exercise 1 demonstrated that there are downsides and costs to direct coupling against external APIs like a filesystem.

One of the classic ways to decouple from an external API is to abstract it into an interface implemented by the API.
In "production" the real API is used while during testing a mock version is substituted.

The version of the scanner in exercise2 uses this strategy, creating a `Filesystem` trait that defines the two operations
the scanner needs from a real filesystem; namely, listing directory contents, and querying the length of files.

The scanner then needs to be parameterized on a Filesystem implementation, which requires passing it to all the code sites where
filesystem operations are used. The [Reader monad is an elegant way to inject dependencies into
code](http://functionaltalks.org/2013/06/17/runar-oli-bjarnason-dead-simple-dependency-injection/) and well supported by
Eff framework.
In fact, the scanner depends upon two external dependencies; not the filesystem but also *topN*, the number of largest files
to report on. So two distinct reader "effects" are combined.

## Tasks

1. Examine the changes to `PathScan.scan` in the Eff version and identify:

-   The return type is now an *Eff expression* `Eff[R, PathScan]`. This is read as *a program that when run has effects described
by the effect set `R` and yields a `PathScan` result*.

-   The effect set `R` passed a type parameter.

-   The *member typeclasses* `_filesystem` and `_config` that denote the effects that `R` must include for this function to
operate (`R` can also include other effects not used in this function).

-   The `for {..} yield` expression in the function body. Eff programs are built by flatMapping over a sequence of sub-steps.
Note the `ask` step that yields `fs` (the current `Filesystem`). Where does the `ask` method come from?


2. Examine `Scanner.pathScan`. Note how we build the Eff program first, then *interpret* (run) it. To run the program, both
`Reader` effects need their dependency provided. Once these effects are resolved, the final call to `run` completes the
program and yields the final result. What happens if you re-order the two calls to `runReader`? What happens if you remove
one call to `runReader`?


3. Extend the use of the Reader effect to `PathScan.takeTopN` and `FileSize.ofFile`. Both of these methods should be converted
to:

- Accept a type parameter `R` and one of the member typeclasses (`_filesystem` and `_config`) denoting the dependency they
need.

- Return their result in an Eff expression

- Use a for-expression internally to `ask` for their dependency, and then `yield` their result.

    If you've made the changes correctly, there shouldn't be any manual passing of the `Filesystem` or `ScanaConfig` parameters.


4. Run the test to verify your changes are working correctly

5. Optional:  Examine the `DefaultFilesystem.listFiles` method and note the try/finally construct. The reason for this is
that `listFiles` returns a `Stream` of the directory contents, which holds open a file handle until the stream is cleaned up.

