FROM amazoncorretto:24
COPY ./device-admin/target/*.jar device-admin.jar
ENTRYPOINT ["java" , "-jar", "device-admin.jar"]