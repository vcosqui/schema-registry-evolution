package io.confluent.examples.clients.basicavro;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.String.valueOf;
import static java.text.MessageFormat.format;

public class ProducerExample {

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) throws IOException {

        final Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream("~/.confluent/java.config")) {
            props.load(inputStream);
        }

        try (KafkaProducer<String, Payment> producer = new KafkaProducer<>(props)) {
            for (long i = 0; i < 10; i++) {
                final String orderId = format("id{0}{1}", valueOf(abs(new Random().nextInt())), i);
                producer.send(
                        new ProducerRecord<String, Payment>("transactions", orderId, new Payment(orderId, new Random().nextDouble())));
                System.out.printf("payment sent with id %s\n", orderId);
                Thread.sleep(1000L);
            }
            producer.flush();
            System.out.printf("Successfully produced 10 messages to a topic called %s%n", "transactions");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
