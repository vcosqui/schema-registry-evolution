package io.confluent.examples.clients.basicavro

import com.google.common.collect.ImmutableList
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.log4j.Logger.getLogger

import java.time.Duration
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.util.Using

object Consumer {

  private val logger = getLogger(getClass)

  @SuppressWarnings(Array("InfiniteLoopStatement"))
  def main(args: Array[String]): Unit = {

    Using(new KafkaConsumer[String, Payment](Env.getProperties)) { consumer =>
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