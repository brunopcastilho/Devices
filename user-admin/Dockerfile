FROM amazoncorretto:24
COPY ./user-admin/target/*.jar user-admin.jar
ENTRYPOINT ["java" , "-jar", "user-admin.jar"]