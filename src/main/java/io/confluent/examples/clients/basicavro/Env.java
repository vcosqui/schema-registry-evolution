package io.confluent.examples.clients.basicavro;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.System.getenv;

public class Env {

    public static Properties getProperties() throws IOException {
        final var props = new Properties();
        try (var config = new FileInputStream("./java.config")) {
            props.load(config);
            props.put("bootstrap.servers", getenv("BOOTSTRAP_SERVERS"));
            props.put("sasl.jaas.config",
                    "org.apache.kafka.common.security.plain.PlainLoginModule required" +
                            " username='"+ getenv("KAFKA_USER")+"' " +
                            "password='"+ getenv("KAFKA_PASSWORD")+"';");
            props.put("schema.registry.url", getenv("SCHEMA_REGISTRY_URL"));
            props.put("basic.auth.user.info", getenv("SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO"));
        }
        return props;
    }
}
