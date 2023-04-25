# Schema registry exercise

## first, configure the environment

Setup java 17 in your env
```shell
 sdk install java 17.0.5-zulu
```
Install maven
```shell
sdk install maven 3.8.6
```

And add the proper security settings and URLs as evn variables
```shell
# broker url and secrets
export BOOTSTRAP_SERVERS=***
export KAFKA_PASSWORD=***
export KAFKA_USER=***
# sr url and secrets
export SCHEMA_REGISTRY_URL=***
export SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO=***
```
## Send and consume some messages and check Schema Registry impact

Create topic `transactions` in Confluent Cloud with default settings

[Payment.asvc](src/main/resources/avro/io/confluent/examples/clients/basicavro/Payment.avsc) initial schema definition

[pom.xml](pom.xml) Check avro plugin configuration

```shell
mvn install
```

[Payment.java](target/generated-sources/io/confluent/examples/clients/basicavro/Payment.java) Check generated java file

[java.config](java.config) Check consumer/producer properties

[Producer](src/main/java/io/confluent/examples/clients/basicavro/ProducerExample.java)/[Consumer](src/main/java/io/confluent/examples/clients/basicavro/ConsumerExample.java), run them.

```shell
mvn install exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ProducerExample
```
> ERROR

Make it fail, check [java.config](java.config) and enable client schema auto registration and try again. Auto schema registration only in DEV

Check Confluent Cloud schema for topic `transactions`
* backwards compatible why? Default new consumers should be able to read ALL
* naming convention topic-key topic-value
          
Try some interactions with the rest API
```shell
curl --silent -X GET -u $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL/subjects/ | jq .
```

```shell
curl --silent -X GET -u $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL/subjects/transactions-value/versions | jq .
```

```shell
curl --silent -X GET -u $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL/subjects/transactions-value/versions/1 | jq .
```

## Schema evolution
you have seen the benefit of **Schema Registry** as being centralized schema management that enables client applications to register and retrieve globally unique schema ids. The main value of Schema Registry, however, is in enabling schema evolution. Similar to how APIs evolve and need to be compatible for all applications that rely on old and new versions of the API, schemas also evolve and likewise need to be compatible for all applications that rely on old and new versions of a schema. This schema evolution is a natural behavior of how applications and data develop over time.
Schema Registry allows for schema evolution and provides compatibility checks to ensure that the contract between producers and consumers is not broken. This allows producers and consumers to update independently and evolve their schemas independently


## BACKWARD COMPATIBILITY
When we have more control of the consumers and have to keep outdated producers working
> Example: IoT devices that are hard to control but control of the consumers (calculate the device position)

Let's put an example, a new business requirements needs the Payments to have a region where they took place.
Is adding a new field a compatible change?
Adding a new field will cause consumers to fail when reading old Payments not having this region.
This change is not backwards compatible.
Let's see how **Schema Registry** can help to identify this invalid evolution.

### FAILING COMPATIBILITY CHECKS

#### fail with maven plugin 
Edit [Payment.asvc](src/main/resources/avro/io/confluent/examples/clients/basicavro/Payment.avsc), add new field
  `{"name": "region", "type": "string"}` and test compatibility:
```shell
  mvn install io.confluent:kafka-schema-registry-maven-plugin:test-compatibility
```
> ERROR

#### fail with rest API

We get same error when using rest API, which is a useful way for non-Java clients to check compatibility from the command line:
```shell
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data '{"schema": "{\"type\":\"record\",\"name\":\"Payment\",\"namespace\":\"io.confluent.examples.clients.basicavro\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"double\"},{\"name\":\"region\",\"type\":\"string\"}]}"}' \
-u $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO \
$SCHEMA_REGISTRY_URL/subjects/transactions-value/versions
```
> ERROR

#### fail with CC UI
Same behaviour in the [CC UI](https://confluent.cloud/)
![](https://docs.confluent.io/platform/current/_images/tutorial-c3-edit-schema-pass.png)

### PASSING COMPATIBILITY CHECKS

Add region attribute with a default
`,{ "name": "region", "type": "string", "default":"UNKNOWN"}`
```shell
  mvn install io.confluent:kafka-schema-registry-maven-plugin:test-compatibility
```
> SUCCESS

The Schema Registry subject for the topic transactions that is called transactions-value has two versions.

## Test Forward compatibility mode
When we have more control of the producers and have to keep outdated consumers working
>Example: ,
for example a reporting app, harder to evolve than the clients producing the events.

CC UI, edit schema "compatibility settings" and set FW

#### fail with maven plugin
Edit [Payment.asvc](src/main/resources/avro/io/confluent/examples/clients/basicavro/Payment.avsc) and remove amount.

```shell
  mvn install io.confluent:kafka-schema-registry-maven-plugin:test-compatibility
```
> ERROR

we cannot delete a field with FW,
updated producers would start creating messages without this field that consumers would not understand.

Instead of deleting amount, we can set a default value `{"name": "amount", "type": "double", "default": 0}` try to check its compatibility with a FW policy

```shell
  mvn install io.confluent:kafka-schema-registry-maven-plugin:test-compatibility
```
> SUCCESS

FW compatibility PREVENTS consumers from reading OLD messages.
This is why FW is not default mode.

## Mention Full compatibility mode
