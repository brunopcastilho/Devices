FROM eclipse-temurin:24.0.1_9-jre-alpine-3.21
COPY ./configserver/target/*.jar configserver.jar
ENTRYPOINT ["java" , "-jar", "configserver.jar", "-spring.profiles.active=docker"]