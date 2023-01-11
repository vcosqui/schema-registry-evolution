# Schema registry exercise

## first, configure JVM clients

```
cat ~/.confluent/java.config
# --------------------------------------
# Confluent Cloud connection information
# --------------------------------------
# ENVIRONMENT_ID=env-12zp95
# SERVICE_ACCOUNT_ID=sa-r06r0p
# KAFKA_CLUSTER_ID=lkc-r5j6v1
# SCHEMA_REGISTRY_CLUSTER_ID=lsrc-2r1y5q
# --------------------------------------
sasl.mechanism=PLAIN
security.protocol=SASL_SSL
bootstrap.servers=<>
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username='<>' password='<>';
basic.auth.credentials.source=USER_INFO
schema.registry.url=<>
basic.auth.user.info=<>
replication.factor=3
## producer settings
acks=all
retries=0
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer

## consumer settings
group.id=test-payments
enable.auto.commit=true
auto.commit.interval.ms=1000
auto.offset.reset=earliest
specific.avro.reader=true
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
value.deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
```
## Send and consume some messages and check Schema Registry impact

* Create topic `transactions` in Confluent Cloud with default settings
* Check
    * `Payment.asvc` initial schema definition
    * `pom.xml` Check avro plugin configuration
    * `Payment.java` Check generated java file
    * `client.properties` show schema reg url, credentials
    * Producer/Consumer, run them
    * Check Confluent Cloud schema for topic `transactions`
        * auto schema registration only in DEV, disable with == auto.register.schemas=false, for PROD use REST interface
        * backwards compatible why? Default new consumers should be able to read ALL
          ```curl -s -X GET -u $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL/subjects/transaction-value | jq .```
          ```curl -s -X GET -u $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL/subjects/transaction-value/versions/latest | jq .```
naming convention topic-key topic-value

## Schema evolution
you have seen the benefit of *Schema Registry* as being centralized schema management that enables client applications to register and retrieve globally unique schema ids. The main value of Schema Registry, however, is in enabling schema evolution. Similar to how APIs evolve and need to be compatible for all applications that rely on old and new versions of the API, schemas also evolve and likewise need to be compatible for all applications that rely on old and new versions of a schema. This schema evolution is a natural behavior of how applications and data develop over time.
Schema Registry allows for schema evolution and provides compatibility checks to ensure that the contract between producers and consumers is not broken. This allows producers and consumers to update independently and evolve their schemas independently

Transitivity
* transitive: ensures compatibility between X-2 <==> X-1 and X-1 <==> X and X-2 <==> X
* non-transitive: ensures compatibility between X-2 <==> X-1 and X-1 <==> X, but not necessarily X-2 <==> X
  Types
* BACKWARD: (default) consumers using the new schema can read data written by producers using the latest registered schema
* BACKWARD_TRANSITIVE: consumers using the new schema can read data written by producers using all previously registered schemas
* FORWARD: consumers using the latest registered schema can read data written by producers using the new schema
* FORWARD_TRANSITIVE: consumers using all previously registered schemas can read data written by producers using the new schema
* FULL: the new schema is forward and backward compatible with the latest registered schema
* FULL_TRANSITIVE: the new schema is forward and backward compatible with all previously registered schemas
* NONE: schema compatibility checks are disabled

BACKWARD COMPATIBILITY
IoT devices that are hard to control but control of the consumers (calculate the device position)
Now we want to evolve the payment entity since a new attribute is required by business
Add new field region Payment2a.asvc
Is this backwards compatible???? Can new consumers read ALL data => NO they will fail, lets see how men plugin can help

FAILING COMPATIBILITY CHECKS

* Pom.xml > sr plugin
  UPDATE ASVC in pom.xml
  mvn io.confluent:kafka-schema-registry-maven-plugin:test-compatibility \
  "-DschemaRegistryUrl=$SCHEMA_REGISTRY_URL" \
  "-DschemaRegistryBasicAuthUserInfo=$SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO"

>>>>>>>>>>>> Execution default-cli of goal io.confluent:kafka-schema-registry-maven-plugin:7.3.1:test-compatibility failed: One or more schemas found to be incompatible with the current version

Not only for maven, Try to register the new schema Payment2a manually to Schema Registry, which is a useful way for non-Java clients to check compatibility from the command line:
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data '{"schema": "{\"type\":\"record\",\"name\":\"Payment\",\"namespace\":\"io.confluent.examples.clients.basicavro\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"double\"},{\"name\":\"region\",\"type\":\"string\"}]}"}' \
-u $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO \
$SCHEMA_REGISTRY_URL/subjects/transactions-value/versions
>>>>>>>>>>>> error_code":409

Try the UI as well, add
,{ "name": "region", "type": "string"}



PASSING COMPATIBILITY CHECKS

Payment2b.asvc
edit in CC UI
,{ "name": "region", "type": "string", "default":"UNKNOWN"}

Think about the registered schema versions. The Schema Registry subject for the topic transactions that is called transactions-value has two schemas:
* version 1 is Payment.avsc
* version 2 is Payment2b.avsc that has the additional field for region with a default empty value.
  UI, Version history


Test Forward compatibility mode
----------- more control of the producers and have to keep consumers always working,
3rd party entity consuming the events, we guarantee that the new events created by producers are compatible

CC UI, edit schema "compatibility settings" and set FW

Check Payment3a.asvc and try to check its compatibility with a FW policy
we cannot delete a field with FW, we have to keep the field but add UNKNOWN default

Check Payment3b.asvc and try to check its compatibility with a FW policy
We can include fields without default
If a payment does not have an amount it's FREE!
Payments now have a product name field that new consumers read only for new messages
FW compatibility PREVENTS consumers from reading OLD messages!!!

This is why FW is not default mode!!!

Mention Full compatibility mode
