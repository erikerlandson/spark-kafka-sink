package org.apache.spark.metrics.sink

import java.util.{ Properties, Locale }
import java.util.concurrent.TimeUnit

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.codahale.metrics.MetricRegistry
import org.apache.spark.SecurityManager

import com.manyangled.kafkasink.KafkaReporter

class KafkaSink(val properties: Properties, val registry: MetricRegistry,
    securityMgr: SecurityManager) extends org.apache.spark.metrics.sink.Sink {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private def popt(prop: String): Option[String] =
    Option(properties.getProperty(prop))

  // These are non-negotiable
  val broker = popt("broker").get
  val topic = popt("topic").get

  lazy val reporter = new KafkaReporter(registry, broker, topic, properties)

  def start(): Unit = {
    logger.info(s"Starting Kafka metric reporter at $broker, topic $topic")
    val period = popt("period").getOrElse("10").toLong
    val tstr = popt("unit").getOrElse("seconds").toUpperCase(Locale.ROOT)
    val tunit = TimeUnit.valueOf(tstr)
    reporter.start(period, tunit)
  }

  def stop(): Unit = {
    logger.info(s"Stopping Kafka metric reporter at $broker, topic $topic")
    reporter.stop()
  }

  def report(): Unit = {
    logger.info(s"Reporting metrics to Kafka reporter at $broker, topic $topic")
    reporter.report()
  }
}
