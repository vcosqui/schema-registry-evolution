package io.confluent.examples.clients.basicavro;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.String.valueOf;
import static java.text.MessageFormat.format;
import static org.apache.log4j.Logger.getLogger;

public class ProducerExample {

    static Logger logger = getLogger(ProducerExample.class);

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) throws IOException {

        try (KafkaProducer<String, Payment> producer = new KafkaProducer<>(Env.getProperties())) {
            for (long i = 0; i < 10; i++) {
                final String orderId = format("id{0}{1}", valueOf(abs(new Random().nextInt())), i);
                Payment order = new Payment(orderId, new Random().nextDouble());
                producer.send(
                        new ProducerRecord<String, Payment>("transactions", orderId, order));
                logger.info("transaction sent with id " + orderId);
                Thread.sleep(1000L);
            }
            producer.flush();
            logger.info("Successfully produced 10 messages to `transactions` topic");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
