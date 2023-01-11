.PHONY: *

build:
	mvn install

produce:
	mvn install exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ProducerExample