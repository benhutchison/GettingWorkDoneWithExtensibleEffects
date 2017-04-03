name := "problem1"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.atnos" %% "eff" % "4.1.0",
  "org.typelevel" %% "cats" % "0.9.0",
  "ws.unfiltered" %% "unfiltered-filter" % "0.9.0",
  "ws.unfiltered" %% "unfiltered-jetty" % "0.9.0"
)

// to write types like Reader[String, ?]
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

// to get types like Reader[String, ?] (with more than one type parameter) correctly inferred for scala 2.12.x
scalacOptions += "-Ypartial-unification"