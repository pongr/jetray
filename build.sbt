organization := "com.pongr"

name := "jetray"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Burtsev" at "http://maven.burtsev.net"
)

libraryDependencies ++= Seq(
  "javax.mail" % "mail" % "1.4.7",
  "com.typesafe.akka" % "akka-actor" % Version.akka,
  "com.google.gdata.gdata-java-client" % "gdata-spreadsheet-3.0" % "1.47.1", //in Burtsev repo
  "com.google.oauth-client" % "google-oauth-client" % "1.14.0-beta",  //in central repo
  "com.typesafe.akka" % "akka-actor" % Version.akka % "runtime",
  "ch.qos.logback" % "logback-classic" % "1.0.9" % "runtime",
  "org.specs2" %% "specs2" % "1.12.4.1" % "test",
  "com.typesafe.akka" % "akka-testkit" % Version.akka % "test"
)

seq(sbtrelease.Release.releaseSettings: _*)

//http://www.scala-sbt.org/using_sonatype.html
//https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots/")
  else                             Some("releases" at nexus + "service/local/staging/deploy/maven2/")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0"))

homepage := Some(url("http://github.com/pongr/jetray"))

organizationName := "Pongr"

organizationHomepage := Some(url("http://pongr.com"))

description := "Scala toolkit for concurrent email load generation, delivery and analysis"

pomExtra := (
  <scm>
    <url>git@github.com:pongr/jetray.git</url>
    <connection>scm:git:git@github.com:pongr/jetray.git</connection>
  </scm>
  <developers>
    <developer>
      <id>zcox</id>
      <name>Zach Cox</name>
      <url>http://theza.ch</url>
    </developer>
  </developers>
)
