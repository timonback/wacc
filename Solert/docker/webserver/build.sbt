name := "wacc"
dockerRepository := Some("timonback")

version := "webserver"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers += "Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1",
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.1"
)



fork in run := true

fork in run := true

fork in run := true