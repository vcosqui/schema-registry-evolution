package io.confluent.examples.clients.basicavro;

import com.google.common.collect.ImmutableList;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public class ConsumerExample {

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) throws IOException {

        final Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream("~/.confluent/java.config")) {
            props.load(inputStream);
        }

        try (final KafkaConsumer<String, Payment> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(ImmutableList.of("transactions"));
            while (true) {
                final ConsumerRecords<String, Payment> records = consumer.poll(Duration.ofMillis(100));
                for (final ConsumerRecord<String, Payment> record : records) {
                    final String key = record.key();
                    final Payment value = record.value();
                    System.out.printf("key = %s, value = %s%n", key, value);
                }
            }
        }
    }
}
