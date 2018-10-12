FROM openjdk:8-jre-alpine
ARG JAR_FILE
COPY target/${JAR_FILE} a.jar
#COPY target/ReportsMaven-1.0-SNAPSHOT-jar-with-dependencies.jar .
ENTRYPOINT ["java","-jar", "a.jar"]
