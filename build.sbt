name := "GettingWorkDoneWithExtensibleEffects"

version := "0.1"

ThisBuild / scalaVersion := "2.12.5"

val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    "org.typelevel" %% "cats-core" % "1.1.0",
    "org.typelevel" %% "mouse" % "0.16",
    "io.monix" %% "monix-eval" % "2.3.3",
    "io.monix" %% "monix-cats" % "2.3.3",
    "org.atnos" %% "eff" % "5.1.0-20180423013334-197fcd1",
    "org.atnos" %% "eff-monix" % "5.1.0-20180423013334-197fcd1",
    "org.atnos" %% "eff-cats-effect" % "5.1.0-20180423013334-197fcd1",
    "org.specs2" %% "specs2-core" % "4.0.3" % "test"
  ),
  // to write types like Reader[String, ?]
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
  //to allow tuple extraction and type ascription in for expressions
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.0"),
  // to get types like Reader[String, ?] (with more than one type parameter) correctly inferred for scala 2.12.x
  scalacOptions += "-Ypartial-unification",
  scalacOptions in Test += "-Yrangepos"
)

lazy val exercise1 = (project in file("exercise1")).settings(commonSettings)

lazy val exercise2 = (project in file("exercise2")).settings(commonSettings)

lazy val exercise3 = (project in file("exercise3")).settings(commonSettings)

lazy val exercise4 = (project in file("exercise4")).settings(commonSettings)

lazy val exercise5 = (project in file("exercise5")).settings(commonSettings)

lazy val solutionExercise1 = (project in file("solutions/exercise1")).settings(commonSettings)

lazy val solutionExerciseClassic = (project in file("solutions/exerciseClassic")).settings(commonSettings)

lazy val solutionExerciseTask = (project in file("solutions/exerciseTask")).settings(commonSettings)

lazy val solutionExerciseReader = (project in file("solutions/exerciseReader")).settings(commonSettings)

lazy val solutionExerciseError = (project in file("solutions/exerciseError")).settings(commonSettings)

lazy val solutionExerciseWriter = (project in file("solutions/exerciseWriter")).settings(commonSettings)

lazy val solutionExerciseState = (project in file("solutions/exerciseState")).settings(commonSettings)

lazy val solutionExercise2 = (project in file("solutions/exercise2")).settings(commonSettings)

lazy val solutionExercise2io = (project in file("solutions/exercise2io")).settings(commonSettings)

lazy val solutionExercise3 = (project in file("solutions/exercise3")).settings(commonSettings)

lazy val solutionExercise4 = (project in file("solutions/exercise4")).settings(commonSettings)

lazy val solutionExercise5 = (project in file("solutions/exercise5")).settings(commonSettings)

val testSolutions = TaskKey[Unit]("testSolutions", "Run all solution tests")
testSolutions := Seq(
  solutionExercise1 / Test / test,
  solutionExercise2 / Test / test,
  solutionExercise3 / Test / test,
  solutionExercise4 / Test / test,
  solutionExercise5 / Test / test,
).dependOn.value

