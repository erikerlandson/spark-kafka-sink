package com.manyangled.kafkasink

import java.util.concurrent.TimeUnit
import java.util.{Date, Properties}
import java.util.Map.Entry

import scala.collection.JavaConverters._
import scala.language.existentials

import scala.util.{ Try, Success, Failure }

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import com.codahale.metrics._

class KafkaReporter(
    registry: MetricRegistry,
    kafkaEndpoint: String,
    kafkaTopic: String)
  extends ScheduledReporter(
    registry,
    "kafka-reporter",
    MetricFilter.ALL,
    TimeUnit.SECONDS,
    TimeUnit.MILLISECONDS) {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  var producer: Option[KafkaProducer[String, String]] = None

  override def start(period: Long, unit: TimeUnit): Unit = {
    super.start(period, unit)
    val status = for {
      kp <- Try {
        logger.info(s"Opening Kafka endpoint $kafkaEndpoint")
        val props = new Properties()
        props.put("bootstrap.servers", kafkaEndpoint)
        props.put("client.id", "KafkaReporter")
        props.put("acks", "all")
        props.put("retries", "0")
        props.put("request.timeout.ms", "5")
        props.put("max.block.ms", "5")
        props.put("key.serializer",
          "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer",
          "org.apache.kafka.common.serialization.StringSerializer")
        new KafkaProducer[String, String](props)
      }
    } yield { kp }
    status match {
      case Success(kp) => {
        logger.info(s"Kafka producer connected to $kafkaEndpoint")
        producer = Some(kp)
      }
      case Failure(err) => {
        logger.error(s"Failure opening Kafka endpoint $kafkaEndpoint:\n$err")
      }
    }
  }

  def report(
    gauges: java.util.SortedMap[String, Gauge[_]],
    counters: java.util.SortedMap[String, Counter],
    histograms: java.util.SortedMap[String, Histogram],
    meters: java.util.SortedMap[String, Meter],
    timers: java.util.SortedMap[String, Timer]): Unit = {

    if (producer.isEmpty) {
      logger.error(s"Missing broker at $kafkaEndpoint: metric output ignored") 
    } else {
      // dump metric output to the kafka topic
      val prod = producer.get
      for { entry <- gauges.entrySet().asScala } {
        gaugeJSON(entry.getValue()).foreach { jv => prod.send(metricRec(entry.getKey(), jv)) }
      }
    }
  }

  private def metricRec(key: String, value: String) =
    new ProducerRecord[String, String](kafkaTopic, key, value)

  private def gaugeJSON(gauge: Gauge[_]): Option[String] = {
    val tpe = ("type" -> "gauge")
    gauge.getValue() match {
      case v: Int => Some(compact(render(tpe ~ ("value" -> v))))
      case v: Long => Some(compact(render(tpe ~ ("value" -> v))))
      case v: Float => Some(compact(render(tpe ~ ("value" -> v))))
      case v: Double => Some(compact(render(tpe ~ ("value" -> v))))
      case v => {
        logger.warn(s"Ignoring unexpected Gauge value: $v")
        None
      }
    }
  }
}
