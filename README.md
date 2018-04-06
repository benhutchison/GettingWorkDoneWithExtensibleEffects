# Getting Work Done With Extensible Effects

YOW Lambdajam Sydney 2017 workshop on programming with the [Eff framework](https://github.com/atnos-org/eff).


The workshop consists of a series of practical exercises which teach the use of Eff. Each exercise is an alternate implementation
of the same use case:

*Ever had a full disk? Where does the space go? Implement a program that can find the largest N files in a directory tree*

## Setup

- Wifi/Internet required.

- You will need Java 8+ and Simple Build Tool (`sbt`) [installed](http://www.scala-sbt.org/release/docs/Setup.html).

- While SBT will download Scala and the Eff libraries on-demand, this can be a slow process. Before the workshop, it is recommended
to run `sbt update` in the base directory to pre-download the required libraries. This may take a few minutes up to 1 hour,
depending what you have cached locally in `~/.ivy2/cache`.

- Import the base SBT project into your IDE: [Intellij](https://www.jetbrains.com/help/idea/2016.1/creating-and-running-your-scala-application.html),
[Eclipse ScalaIDE](http://scala-ide.org/) or [Ensime](http://ensime.org/).

- Or work with any editor and the SBT command line if you prefer.

  *Be warned that IDE presentation compilers don't correctly handle some Eff code*, and may
flag valid code as invalid. Try your code with the full Scala compiler via SBT command line before concluding there is a problem.

## Exercises

The SBT base project contains five exercise projects, each with a README with instructions to attempt. Each of them contains
a different implementation of a file scanner program. Do the exercises in order.

The instruction pages are best viewed in a browser; reach them here:
- [exercise1](exercise1/README.md) - File Scanning without using Eff
- [exercise2](exercise2/README.md) - Using Eff Reader effect for dependency injection
- [exercise3](exercise3/README.md) - Using Eff Either effect for error handling
- [exercise4](exercise4/README.md) - Using Eff Task effect for asynchronous & concurrent execution
- [exercise5](exercise5/README.md) - Using Eff Writer effect for logging

There are three types of tasks you'll encounter
- :mag: _Study Code_ Study existing application and test code
- :pencil: _Write Code_ Adding missing code or changing existing code at an indicated line or method.
- :arrow_forward: _Run Code_ Run the file scanner (eg `exercise1/run`) or the unit tests (eg `exercise1/test`) from SBT prompt.

Each project can be compiled, run or tested separately; errors in one project won't affect the others.

## Solutions

There is a [solutions](solutions/) subfolder containing 5 corresponding solution subprojects.

There is learning value in attempting a hard problem, getting stuck, then reviewing the solution.
Use the solutions if you get blocked!

## Using SBT

Start SBT in the base directory and then operate from the SBT prompt. Invoking each
SBT command from the shell (eg `sbt exercise1/compile`) is slower due to JVM startup costs.
```
/Users/ben_hutchison/projects/GettingWorkDoneWithExtensibleEffects $ sbt
Getting org.scala-sbt sbt 0.13.13 ...
..further sbt loading omitted..
>
```

To list all 5 exercise and 5 solution subproject names:
```
> projects
```

Try running the file scanner (ie `main` method) of subproject `solutionExercise1` on the current directory.
```
> solutionExercise1/run .`
```

To compile sources in subproject `exercise1`:
```
> exercise1/compile`
```

To run any unit tests (in `src/test/scala/*`) under subproject `exercise1`
```
> exercise1/test
```


*SBT commands should be scoped to a subproject (eg `exercise1/test`). Running eg `test` at the top level will load 10 copies of the classes into the SBT JVM, potentially leading to `OutOfMemoryError: Metaspace`*


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

## Version History

May 2017

* Initial version for Lambdajam 2017, based on Eff 4.3.1, cats 0.9.0 and Monix 2.2.4. Includes 5 exercises introducing
`Reader`, `Either`, `Task` and `Writer` effects.

May 2018

* Upgrade libraries to Eff 5.1, cats 1.1, sbt 1.1 and introduce Cats Effect library to use IO effect rather than Task.



