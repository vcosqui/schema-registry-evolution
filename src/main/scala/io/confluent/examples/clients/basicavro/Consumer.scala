package io.confluent.examples.clients.basicavro

import com.google.common.collect.ImmutableList
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.log4j.Logger.getLogger

import java.io.FileInputStream
import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.util.Using

object Consumer {

  private val logger = getLogger(getClass)

  @SuppressWarnings(Array("InfiniteLoopStatement"))
  def main(args: Array[String]): Unit = {

    val props = new Properties
    Using(new FileInputStream("./java.config")) { inputStream =>
      props.load(inputStream)
    }

    Using(new KafkaConsumer[String, Payment](props)) { consumer =>
      consumer.subscribe(ImmutableList.of("transactions"))
      while (true) {
        for (record <- consumer.poll(Duration.ofMillis(100)).asScala) {
          val key = record.key
          val value = record.value
          logger.info(s"key = $key, value = $value")
        }
      }
    }
  }
}