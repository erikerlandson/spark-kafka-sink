name := "spark-kafka-sink"

organization := "com.manyangled"

version := "0.1.0"

scalaVersion := "2.11.8"

val SPARK_VERSION = "2.1.0"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-nop" % "1.7.16" % Provided,
  "org.json4s" %% "json4s-jackson" % "3.2.11" % Provided,
  "org.apache.kafka" % "kafka-clients" % "0.10.2.0",
  "org.apache.kafka" % "kafka_2.11" % "0.10.2.0" % Provided,
  "io.dropwizard.metrics" % "metrics-core" % "3.1.2" % Provided,
  "org.apache.spark" %% "spark-core" % SPARK_VERSION % Provided,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)

licenses += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/root-doc.txt")

test in assembly := {}

assemblyShadeRules in assembly := Seq(
  ShadeRule.zap("scala.**").inAll,
  ShadeRule.zap("org.slf4j.**").inAll,
  ShadeRule.zap("org.apache.kafka.clients.consumer.**").inAll
)
