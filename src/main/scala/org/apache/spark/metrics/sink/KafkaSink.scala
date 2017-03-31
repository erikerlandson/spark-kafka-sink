package org.apache.spark.metrics.sink

import java.util.Properties
import com.codahale.metrics.MetricRegistry
import org.apache.spark.SecurityManager

class KafkaSink(val property: Properties, val registry: MetricRegistry,
    securityMgr: SecurityManager) extends org.apache.spark.metrics.sink.Sink {
  def start(): Unit = {
    println(s"STARTING KAFKA SINK")
  }

  def stop(): Unit = {
    println(s"STOPPING KAFKA SINK")
  }

  def report(): Unit = {
    println(s"REPORTING THROUGH KAFKA SINK")
  }
}
