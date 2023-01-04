FROM openjdk:8
WORKDIR /
ADD target/*.jar /
RUN ls -ltr
EXPOSE 8080
CMD java -cp "queueoke-jar-with-dependencies.jar" com.demo.consumer.QueueConsumer
