name := "ProvFuzz"
version := "1.0"
scalaVersion := "2.11.8"

libraryDependencies += "org.scalameta" %% "scalameta" % "4.2.3"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.3"
libraryDependencies += "org.roaringbitmap" % "RoaringBitmap" % "0.8.11"
libraryDependencies += "org.scoverage" %% "scalac-scoverage-plugin" % "1.4.1"
libraryDependencies += "org.scoverage" %% "scalac-scoverage-runtime" % "1.4.1"
libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.11.8",
  "org.scala-lang" % "scala-reflect" % "2.11.8"
)
