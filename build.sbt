organization := "com.pongr"

name := "jetray"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "javax.mail" % "mail" % "1.4.7",
  "com.typesafe.akka" % "akka-actor" % Version.akka,
  "com.typesafe.akka" % "akka-actor" % Version.akka % "runtime",
  "ch.qos.logback" % "logback-classic" % "1.0.9" % "runtime",
  "org.specs2" %% "specs2" % "1.12.4.1" % "test",
  "com.typesafe.akka" % "akka-testkit" % Version.akka % "test"
)
