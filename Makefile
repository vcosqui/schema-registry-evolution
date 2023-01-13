.PHONY: *

build:
	mvn install

produce-java:
	mvn install exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ProducerExample

consume-java:
	mvn install exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ConsumerExample

produce-scala:
	mvn install exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.Producer

consume-scala:
	mvn install exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.Consumer
