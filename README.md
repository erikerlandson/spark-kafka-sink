## spark-kafka-sink
A Kafka metric sink for Apache Spark

### Quick and dirty start

#### Stand up a local kafka broker (If you need one)
```bash
# install docker-compose
% sudo dnf -y install docker-compose
# Download kafka-docker git repo
% git clone https://github.com/wurstmeister/kafka-docker
% cd kafka-docker
# edit file docker-compose-single-broker.yml to
# configure the value of KAFKA_ADVERTISED_HOST_NAME env var
# (setting advertised host name to 127.0.0.1 worked for me running locally)
# The following should build a local image and spin up a kafka (and zookeeper),
% docker-compose -f docker-compose-single-broker.yml up
# You should now have a running kafka broker accessible at 127.0.0.1:9092
# (and zookeeper at 127.0.0.1:2181)
```

#### Download and build a kafka-sink uber-jar
```bash
% git clone https://github.com/erikerlandson/spark-kafka-sink.git
% cd spark-kafka-sink
# Using xsbt instead of sbt may work more smoothly
% sbt assembly
# you should now have an uber ("assembly") jar in:
# /path/to/spark-kafka-sink/target/scala-2.11/spark-kafka-sink-assembly-0.1.0.jar
```

#### Configure your spark metrics.properties file
Edit `/path/to/spark/conf/metrics.properites` to look like this:
```
master.source.jvm.class=org.apache.spark.metrics.source.JvmSource
worker.source.jvm.class=org.apache.spark.metrics.source.JvmSource
driver.source.jvm.class=org.apache.spark.metrics.source.JvmSource
executor.source.jvm.class=org.apache.spark.metrics.source.JvmSource

*.sink.kafka.class=org.apache.spark.metrics.sink.KafkaSink
*.sink.kafka.broker=127.0.0.1:9092
*.sink.kafka.topic=test
*.sink.kafka.period=10
*.sink.kafka.unit=seconds

# histquantiles and timerquantiles have following defaults:
#*.sink.kafka.histquantiles=0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0
#*.sink.kafka.timerquantiles=0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0

# These carry configure settings to the KafkaProducer
# *.sink.kafka.prodconf_xxx, where xxx can be anything, just has to
# be unique per setting:
*.sink.kafka.prodconf_a=retries=0
*.sink.kafka.prodconf_b=acks=all
*.sink.kafka.prodconf_c=request.timeout.ms=5
*.sink.kafka.prodconf_d=max.block.ms=5
```

#### Run a spark-shell with kafka metric sink plugin

Make sure the kafka-sink jar file is on spark's classpath.  This simple test adds the jar
using the `--jars` argument:
```bash
% cd /path/to/spark
% ./bin/spark-shell --master=local[*] --jars=/path/to/spark-kafka-sink/target/scala-2.11/spark-kafka-sink-assembly-0.1.0.jar
```
If you run a Kafka consumer listening on topic `test` at `127.0.0.1:9092`, you should
start to see metrics appearing.  The metric names are the kafka message key. Values are
JSON, representing metric structures.  If you have [apache kafka](https://github.com/apache/kafka)
installed, you can test a simple consumer on the command line:
```bash
% kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic test --from-beginning
```
(This CLI consumer does not print out the message keys, so you will only see the values)


