FROM openjdk:8-jre-alpine
ARG JAR_FILE
COPY target/${JAR_FILE} a.jar
COPY target/google.drive.delete.permission-1.0.jar .
ENTRYPOINT ["java","-jar", "a.jar"]