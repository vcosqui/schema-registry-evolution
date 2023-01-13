# Schema registry exercise

## first, configure JVM clients

And add the proper security settings and URLs as evn variables
```shell
export KAFKA_PASSWORD=***
export KAFKA_USER=***
export SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO=***
export SCHEMA_REGISTRY_URL=***
export BOOTSTRAP_SERVERS=***
```
## Send and consume some messages and check Schema Registry impact

* Create topic `transactions` in Confluent Cloud with default settings
* Check
    * `Payment.asvc` initial schema definition
    * `pom.xml` Check avro plugin configuration
    * `Payment.java` Check generated java file
    * `client.properties` show schema reg url, credentials
    * Producer/Consumer, run them `make produce-java` `make consume-java`
    * Check Confluent Cloud schema for topic `transactions`
        * auto schema registration only in DEV, disable with == auto.register.schemas=false, for PROD use REST interface
        * backwards compatible why? Default new consumers should be able to read ALL
        * naming convention topic-key topic-value
          
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
you have seen the benefit of *Schema Registry* as being centralized schema management that enables client applications to register and retrieve globally unique schema ids. The main value of Schema Registry, however, is in enabling schema evolution. Similar to how APIs evolve and need to be compatible for all applications that rely on old and new versions of the API, schemas also evolve and likewise need to be compatible for all applications that rely on old and new versions of a schema. This schema evolution is a natural behavior of how applications and data develop over time.
Schema Registry allows for schema evolution and provides compatibility checks to ensure that the contract between producers and consumers is not broken. This allows producers and consumers to update independently and evolve their schemas independently


## BACKWARD COMPATIBILITY
> Example: when we have more control of the consumers and have to keep outdated producers working, IoT devices that are hard to control but control of the consumers (calculate the device position)

Now we want to evolve the payment entity since a new attribute is required by business
Add new field region Payment2a.asvc
Is this backwards compatible???? Can new consumers read ALL data => NO they will fail, lets see how men plugin can help

### FAILING COMPATIBILITY CHECKS

#### fail with maven plugin 
  UPDATE Payment.asvc with new field
  `{"name": "region", "type": "string"}`
```shell
  mvn io.confluent:kafka-schema-registry-maven-plugin:test-compatibility \
  "-DschemaRegistryUrl=$SCHEMA_REGISTRY_URL" \
  "-DschemaRegistryBasicAuthUserInfo=$SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO"
```
> Execution default-cli of goal io.confluent:kafka-schema-registry-maven-plugin:7.3.1:test-compatibility failed: One or more schemas found to be incompatible with the current version

#### fail with rest API

Not only for maven, Try to register the new schema Payment2a manually to Schema Registry, which is a useful way for non-Java clients to check compatibility from the command line:
```shell
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data '{"schema": "{\"type\":\"record\",\"name\":\"Payment\",\"namespace\":\"io.confluent.examples.clients.basicavro\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"double\"},{\"name\":\"region\",\"type\":\"string\"}]}"}' \
-u $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO \
$SCHEMA_REGISTRY_URL/subjects/transactions-value/versions
```
> http error_code: 409

#### fail with CC UI
Try the UI

### PASSING COMPATIBILITY CHECKS

Add region attribute with a default
`,{ "name": "region", "type": "string", "default":"UNKNOWN"}`

Think about the registered schema versions. The Schema Registry subject for the topic transactions that is called transactions-value has two schemas:
* version 1 is Payment.avsc
* version 2 is Payment2b.avsc that has the additional field for region with a default empty value.
  CC UI, Version history

## Test Forward compatibility mode
>Example: when we have more control of the producers and have to keep outdated consumers working,
for example a 3rd party entity consuming the events, we guarantee that the new events created by producers are always compatible.

CC UI, edit schema "compatibility settings" and set FW

Check Payment.asvc and remove amount
we cannot delete a field with FW, we have to keep the field but add UNKNOWN default
updated producers would start creating messages without this filed that consumers would not understand

Instead of deleting amount, we can set a default value `{"name": "amount", "type": "double", "default": 0}` try to check its compatibility with a FW policy
We can include fields without default
If a payment does not have an amount it's FREE!
Payments now have a product name field that new consumers read only for new messages
FW compatibility PREVENTS consumers from reading OLD messages!!!
This is why FW is not default mode!!!

## Mention Full compatibility mode
