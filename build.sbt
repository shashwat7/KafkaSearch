import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

name := "KafkaSearch"

version := "1.0"

scalaVersion := "2.10.4"

val kafkaVersion = "0.9.0.1"

// https://mvnrepository.com/artifact/org.apache.kafka/kafka_2.10
libraryDependencies += "org.apache.kafka" %% "kafka" % kafkaVersion

// https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
libraryDependencies += "org.apache.kafka" % "kafka-clients" % kafkaVersion

// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.23"

// https://mvnrepository.com/artifact/org.mapdb/mapdb
//libraryDependencies += "org.mapdb" % "mapdb" % "3.0.3"
libraryDependencies += "org.mapdb" % "mapdb" % "1.0.9"

// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-client
// libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.6.0"

resolvers ++= Seq(Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snaspshots"),
  "cloudera"           at "http://repository.cloudera.com/content/repositories/releases",
  "Scalaz Bintray Repo"  at "http://dl.bintray.com/scalaz/releases",
  Resolver.mavenLocal)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) => MergeStrategy.discard
      case _ => MergeStrategy.discard
    }
  case _ => MergeStrategy.first
}

javacOptions in Compile ++= Seq("-source", "1.7",  "-target", "1.7")
javaOptions ++= Seq("-Xms512M", "-Xmx8192M", "-XX:MaxPermSize=8192M", "-XX:+CMSClassUnloadingEnabled")

scalacOptions += "-target:jvm-1.7"

scalacOptions += "-feature"

packageArchetype.java_application

net.virtualvoid.sbt.graph.Plugin.graphSettings