package io.confluent.examples.clients.basicavro


import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.log4j.Logger.getLogger

import scala.language.postfixOps
import scala.util.{Random, Using}

object Producer {

  private val logger = getLogger(getClass)

  def main(args: Array[String]): Unit = {

    Using(new KafkaProducer[String, Payment](Env.getProperties)) { producer =>
      1 to 10 foreach { i =>
        val orderId = s"id${Random.alphanumeric take 10 mkString}$i"
        val payment = new Payment(orderId, new Random().nextDouble)
        producer.send(new ProducerRecord[String, Payment]("transactions", orderId, payment))
        logger.info("payment sent with id " + orderId)
        Thread.sleep(1000L)
      }
      producer.flush()
      logger.info("Successfully produced 10 messages to `transactions` topic")
    }
  }
}
