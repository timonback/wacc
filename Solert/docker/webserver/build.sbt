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
  "datastax" % "spark-cassandra-connector" % "1.6.0-s_2.10",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14"
)

routesGenerator := InjectedRoutesGenerator


fork in run := true

fork in run := true

fork in run := true

fork in run := true