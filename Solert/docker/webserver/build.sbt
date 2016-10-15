name := "wacc"
dockerRepository := Some("timonback")

version := "webserver-cassandra"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers += "Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven"
//phantom dsl
resolvers ++= Seq(
  "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  Resolver.bintrayRepo("websudos", "oss-releases")
)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.0",
  "com.websudos" %% "phantom-dsl" % "1.29.3",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14"
)

routesGenerator := InjectedRoutesGenerator


fork in run := true

fork in run := true

fork in run := true


fork in run := true

fork in run := true

fork in run := true