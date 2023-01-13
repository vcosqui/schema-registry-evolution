package io.confluent.examples.clients.basicavro;

import com.google.common.collect.ImmutableList;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.apache.log4j.Logger.getLogger;

public class ConsumerExample {

    static Logger logger = getLogger(ProducerExample.class);

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) throws IOException {

        final Properties props = new Properties();
        try (InputStream config = new FileInputStream("./java.config")) {
            props.load(config);
        }

        try (final KafkaConsumer<String, Payment> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(ImmutableList.of("transactions"));
            while (true) {
                final ConsumerRecords<String, Payment> records = consumer.poll(ofMillis(100));
                for (final ConsumerRecord<String, Payment> record : records) {
                    final String key = record.key();
                    final Payment value = record.value();
                    logger.info("consumed message with key: " + key);
                }
            }
        }
    }
}
