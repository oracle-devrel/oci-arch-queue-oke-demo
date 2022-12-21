set queue_profile=LOCAL
java -cp "queue-samples-jar-with-dependencies.jar" com.demo.samples.basic.QueueProducer
java -cp "queue-samples-jar-with-dependencies.jar" com.demo.samples.basic.GetStats
mvn --quiet exec:java -Dexec.mainClass=com.demo.samples.basic.QueueProducer