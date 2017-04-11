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

  private def popt(prop: String): Option[String] =
    Option(properties.getProperty(prop))

  lazy val reporter = new KafkaReporter(
    registry,
    popt("broker").get,
    popt("topic").get)

  def start(): Unit = {
    println(s"STARTING KAFKA SINK")
    println(s"KafkaSink properties:\n$properties")
    val period = popt("period").getOrElse("10").toLong
    val tstr = popt("unit").getOrElse("seconds").toUpperCase(Locale.ROOT)
    val tunit = TimeUnit.valueOf(tstr)
    reporter.start(period, tunit)
  }

  def stop(): Unit = {
    println(s"STOPPING KAFKA SINK")
    reporter.stop()
  }

  def report(): Unit = {
    println(s"REPORTING THROUGH KAFKA SINK")
    reporter.report()
  }
}
