package io.confluent.examples.clients.basicavro;

import com.google.common.collect.ImmutableList;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.io.IOException;

import static io.confluent.examples.clients.basicavro.Env.getProperties;
import static java.lang.String.format;
import static java.time.Duration.ofMillis;
import static org.apache.log4j.Logger.getLogger;

public class ConsumerExample {

    static Logger logger = getLogger(ProducerExample.class);

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) throws IOException {

        try (final var consumer = new KafkaConsumer<String, Payment>(getProperties())) {
            consumer.subscribe(ImmutableList.of("transactions"));
            while (true) {
                final var records = consumer.poll(ofMillis(100));
                for (final var record : records) {
                    final var key = record.key();
                    final var value = record.value();
                    logger.info(format("consumed message with key %s value %s", key, value));
                }
            }
        }
    }
}
