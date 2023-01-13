.PHONY: *

build:
	mvn install

produce-java:
	mvn install exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ProducerExample

produce-scala:
	mvn install exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.Producer
