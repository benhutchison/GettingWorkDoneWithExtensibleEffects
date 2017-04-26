# Getting Work Done With Extensible Effects

YOW Lambdajam Sydney 2017 workshop on programming with the [Eff framework](https://github.com/atnos-org/eff).


The workshop consists of a series of practical exercises which teach the use of Eff. Each exercise is an alternate implementation
of the same use case:

*Ever had a full disk? Where does the space go? Implement a program that can find the largest N files in a directory tree*

The first implementation doesn't use Eff at all. Each subsequent exercise layers more Eff features into the code.

## Setup

- You will need Java 8+ and Simple Build Tool (`sbt`) [installed](http://www.scala-sbt.org/release/docs/Setup.html).

- While SBT will download Scala and the Eff libraries on-demand, this can be a slow process. Before the workshop, it is recommended
to run `sbt update` in the base directory to pre-download the required libraries. This may take a few minutes up to 1 hour,
depending what you have cached locally in `~/.ivy2/cache`.

- The base SBT project can be imported into IDEs [Intellij](https://www.jetbrains.com/help/idea/2016.1/creating-and-running-your-scala-application.html),
[Eclipse ScalaIDE](http://scala-ide.org/) or [Ensime](http://ensime.org/). Or work with any editor and the SBT command line.

  If you do choose to use an IDE, *be warned that IDE presentation compilers don't correctly handle some Eff code*, and may well
flag valid code as invalid. Try your code with the full Scala compiler via SBT command line before concluding there is a problem.

## Layout of Exercises

It is arranged as an SBT "container" project,
which contains a number of child projects, named `exercise1`, `exercise2` etc. Attempt them in order.

*Each subproject has a [README.md](exercise1/README.md) with instructions to attempt*. You will need to carefully study the provided code and tests,
 and potentially read the Eff documentation, to complete the exercises.

Each can be compiled, run or tested separately;
compile errors in one project won't affect the others.

While doing the workshop, run SBT in the base directory with the command `sbt` and then operate from the SBT prompt. Invoking each
SBT command from the shell (eg `sbt exercise1/compile`) *will* be slower due to JVM startup costs.

- To list all subprojects, use SBT command `projects`

- To compile sources in subproject `exercise1`, use SBT command `exercise1/compile`

- To run any unit tests in `src/test/scala/*` under subproject `exercise1`, use SBT command `exercise1/test`

- To run the main method of subproject `exercise1`, use SBT command `exercise1/run <program arguments>`

*SBT commands should be scoped to a subproject (eg `exercise1/test`). Running eg `test` at the top level will load 10 copies of the classes into the SBT JVM, potentially leading to `OutOfMemoryError: Metaspace`*

## Solutions

There is a `solutions/` subfolder containing solution subprojects. There is learning value in attempting a hard problem,
getting stuck, then reviewing the solution. So make use of the solutions if you find yourself blocked.

## "Learn by Doing"

This project teaches Extensible Effects in practice; what it feels like to code with the Eff framework.

It doesn't make any attempt to cover
the complex, subtle theory behind Eff, a refinement of 25 years experience of programming with monads, and isn't a complete picture of Eff
by any means. At the time of writing however, there are more resources available covering the theory, than practice, of Eff, including:

- The original paper [Extensible effects: an alternative to monad transformers](https://www.cs.indiana.edu/~sabry/papers/exteff.pdf)
in Haskell and followup refinement [Freer Monads, More Extensible Effects](http://okmij.org/ftp/Haskell/extensible/more.pdf).

- [Video presentation](https://www.youtube.com/watch?v=3Ltgkjpme-Y) of the above material by Oleg Kiselyov

- [The Eff monad, one monad to rule them all](https://www.youtube.com/watch?v=KGJLeHhsZBo) by Eff library creator Eric Torreborre

- My own video [Getting Work Done with the Eff Monad in Scala](https://www.youtube.com/watch?v=LhGq4HlozV4)
