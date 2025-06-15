FROM eclipse-temurin:24.0.1_9-jre-alpine-3.21
COPY ./eurekaserver/target/*.jar eurekaserver.jar
ENTRYPOINT ["java" , "-jar", "eurekaserver.jar"]