# Transforming Effects with Functions & Optics

In this exercise we'll look at how we can transform within effect "families" using functions and Monocle optics.

We have seen several examples of effects which have a second type parameter in addition to the payload:

- `Either[E, ?]` has the type of the error `E`
- `Reader[A, ?]` has the type of value to be provided/injected `A`
- `State[S, ?]` has the type of the state `S`

We can think of these effects as being families of related effects, sharing the same basic mechanism but with a particular
type focus. But, eg  `Reader[A, ?]` and `Reader[B, ?]`, are different effects in the Eff effect stack and can't be combined.

The recommended approach is where possible, to work in terms of one effect from an effect family across your program
(ie one `Reader`, one `State`). This tends to be the most robust and reliable solution from a type inference perspective.
However, it's sometimes necessary or convenient to be able to combine effects from the same family, but with different foci,
together. You may need to integrate modules written by different authors that both focus on types from their own modules,
for example. So we'll look at how you can do that:

- To transform a `Either[E, ?]` into `Either[E, ?]`, we simply need a function `E => E1` and we can `leftMap` it (`Either`
is a *covariant* functor). `Writer` works similarly.

- To transform a `Reader[A, ?]` into `Reader[B, ?]`, we need a function `B => A` to transform the input while we read it
(`Reader` is a *contravariant* functor).

- `State[S, ?]` consumes and emits its `S` value, so a single function won't do. We need a bidirectional transform between
`S` and `T` to produce a  `State[T, ?]` effect. This is what a `Lens[S, T]` is.

In this exercise, we'll look at transforming two `Reader` effects to a common shared type, but the approach should extend
to any effect that has a focus type.

### :mag: _Study Code_

- You should have a conceptual idea of how [Monocle lenses](http://julien-truffaut.github.io/Monocle/optics/lens.html)
work to follow this exercise.


- Recall in previous examples we had two distinct readers for two different type of configuration, the `Filesystem` and
the number, `topN`, of the largest files that the scanner would keep track of. Now, there is a new class `AppConfig` that
models the applications config, has has these two config items as fields. We want to write the overall program purely in
terms of `AppConfig` and transform its `Reader` effect into readers of the other two subtypes.

   Find `AppConfig` and its companion. Note that it has an implicit `Lens` declared between `AppConfig` and `Filesystem`.


- Note that `pathScan` has a single reader effect `_appconfig`, but calls methods like `takeTopN` and `FileSize.ofFile`
that declare different reader effects `_config` and `_filesystem` respectively. So effect transformation is occurring
here implicitly, driven by the type system.


- The mechanism used to transform effects is  `EffOptics.readerLens`. We wants to `transform` the membership typeclasses,
yielding a typeclass that certifies membership for the transformed focus. The `~>` denotes a *natural transformation*,
which is a "higher kinded function" from an `A[_]` to a `B[_]`.

  Note that `EffOptics.readerLens` is marked implicit, so it is wired in by the compiler whenever a method is searching
  for an effect member typeclass. However, `EffOptics.readerLens` itself trails an implicit dependency upon a `Lens[S, T]`
  to be in implicit scope.


### :pencil: _Write Code_

- Complete the implementation of the transform `apply` method in `EffOptics.readerLens`; ie how can you use a `Lens[S, T]`
to build a `Reader[T]` from a `Reader[S, ?]`?

- Add a second lens in `AppConfig` to `ScanConfig`, using Monocle's `GenLens` macro. Ensure its marked `implicit` so it
can passed by the compiler when effects need to be transformed.


### :arrow_forward: Run Code_

Run the unit tests to check that your implementation is correct.



### :mag: _Study Code_

- Why did we use `Lens` and not just `A => B` to transform our `Reader` effects? A function would have worked, as the
bidrectional nature of `Lens` is only required for `State` effects where the change of focus is two-way.

  The reason we avoided function was because we didn't want to depend upon any function `A => B` implicitly. It's a very
  generic type and likely to lead to ambiguity. Lens is a less common type so we could be more confident that there wouldn't be random
  lenses already in implicit scope.