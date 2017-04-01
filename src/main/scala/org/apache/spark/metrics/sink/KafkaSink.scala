package org.apache.spark.metrics.sink

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.codahale.metrics.MetricRegistry
import org.apache.spark.SecurityManager

import com.manyangled.kafkasink.KafkaReporter

class KafkaSink(val property: Properties, val registry: MetricRegistry,
    securityMgr: SecurityManager) extends org.apache.spark.metrics.sink.Sink {

  val reporter = new KafkaReporter(
    registry,
    "bogus-endpoint",
    "bogus-topic")

  def start(): Unit = {
    println(s"STARTING KAFKA SINK")
    reporter.start(5L, TimeUnit.valueOf("SECONDS"))
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
