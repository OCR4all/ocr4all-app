#
# File: Dockerfile
#
# Assembles a Docker image to run ocr4all on a openjdk container.
#
# Author: Herbert Baier (herbert.baier@uni-wuerzburg.de)
# Date: 16.04.2024
#
ARG TAG
FROM openjdk:${TAG}

#
# install required packages
#
RUN apt-get -y update

# install imagemagick
RUN apt-get install -y imagemagick

#
# install application
#
WORKDIR /application

ARG APP_VERSION
COPY target/ocr4all-app-${APP_VERSION}.jar app.jar

#
# start application
#
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]