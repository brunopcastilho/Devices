FROM eclipse-temurin:24.0.1_9-jre-alpine-3.21
COPY ./webserver/target/*.jar webserver.jar
ENTRYPOINT ["java" , "-jar", "webserver.jar"]