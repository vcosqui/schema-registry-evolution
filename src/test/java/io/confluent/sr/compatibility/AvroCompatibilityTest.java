package io.confluent.sr.compatibility;

import io.confluent.kafka.schemaregistry.CompatibilityChecker;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AvroCompatibilityTest {

    private final AvroSchema initial = new AvroSchema("""
            {
             "namespace": "io.confluent.examples.clients.basicavro",
             "type": "record",
             "name": "Payment",
             "fields": [
                 {"name": "id", "type": "string"},
                 {"name": "amount", "type": "double"}
             ]
            }
            """);

    private final AvroSchema regionWithDefault = new AvroSchema("""
            {
             "namespace": "io.confluent.examples.clients.basicavro",
             "type": "record",
             "name": "Payment",
             "fields": [
                 {"name": "id", "type": "string"},
                 {"name": "amount", "type": "double"},
                 {"name": "region", "type": "string", "default": ""}
             ]
            }
            """);

    private final AvroSchema regionWithoutDefault = new AvroSchema("""
            {
             "namespace": "io.confluent.examples.clients.basicavro",
             "type": "record",
             "name": "Payment",
             "fields": [
                 {"name": "id", "type": "string"},
                 {"name": "amount", "type": "double"},
                 {"name": "region", "type": "string"}
             ]
            }
            """);

    @Test
    public void testBasicBackwardsCompatibility() {
        CompatibilityChecker checker = CompatibilityChecker.BACKWARD_CHECKER;
        assertTrue("adding a field with default is a backward compatible change",
                checker.isCompatible(regionWithDefault, singletonList(initial)).isEmpty());
    }

    @Test
    public void testFailBackwardsCompatibility() {
        CompatibilityChecker checker = CompatibilityChecker.BACKWARD_CHECKER;
        assertFalse("adding a field without default is NOT a backward compatible change",
                checker.isCompatible(regionWithoutDefault, singletonList(initial)).isEmpty());
    }

    @Test
    public void testBasicForwardsCompatibility() {
        CompatibilityChecker checker = CompatibilityChecker.FORWARD_CHECKER;
        assertTrue("adding a field is a forward compatible change",
                checker.isCompatible(regionWithDefault, singletonList(initial)).isEmpty());
    }
}