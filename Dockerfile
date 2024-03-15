FROM openjdk:17-jdk-alpine
WORKDIR application

COPY target/ocr4all-app-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]