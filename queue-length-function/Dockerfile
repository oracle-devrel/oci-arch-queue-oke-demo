FROM fnproject/fn-java-fdk:jre11-1.0.150
WORKDIR /function
COPY  target/*.jar /function/app/
RUN ls -R
CMD ["com.example.fn.QueueLength::handleRequest"]
