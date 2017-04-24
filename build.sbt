name := "GettingWorkDoneWithExtensibleEffects"

version := "0.1"

scalaVersion := "2.12.1"

val commonSettings = Seq(
  scalaVersion := "2.12.1",
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    "org.typelevel" %% "cats" % "0.9.0",
    "io.monix" %% "monix-eval" % "2.2.4",
    "io.monix" %% "monix-cats" % "2.2.4",
    "org.atnos" %% "eff" % "4.3.1",
    "org.atnos" %% "eff-monix" % "4.3.1",
    "org.specs2" %% "specs2-core" % "3.8.9" % "test"
  ),
  // to write types like Reader[String, ?]
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
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

lazy val solutionExercise2 = (project in file("solutions/exercise2")).settings(commonSettings)

lazy val solutionExercise3 = (project in file("solutions/exercise3")).settings(commonSettings)

lazy val solutionExercise4 = (project in file("solutions/exercise4")).settings(commonSettings)

lazy val solutionExercise5 = (project in file("solutions/exercise5")).settings(commonSettings)

