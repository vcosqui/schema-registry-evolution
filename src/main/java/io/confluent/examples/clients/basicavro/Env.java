package io.confluent.examples.clients.basicavro;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Env {

    public static Properties getProperties() throws IOException {
        final Properties props = new Properties();
        try (InputStream config = new FileInputStream("./java.config")) {
            props.load(config);
            props.put("bootstrap.servers", System.getenv("BOOTSTRAP_SERVERS"));
            props.put("sasl.jaas.config",
                    "org.apache.kafka.common.security.plain.PlainLoginModule required" +
                            " username='"+System.getenv("KAFKA_USER")+"' " +
                            "password='"+System.getenv("KAFKA_PASSWORD")+"';");
            props.put("schema.registry.url", System.getenv("SCHEMA_REGISTRY_URL"));
            props.put("basic.auth.user.info", System.getenv("SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO"));
        }
        return props;
    }
}
