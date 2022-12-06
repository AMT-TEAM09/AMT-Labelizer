# syntax = docker/dockerfile:1.2
FROM ubuntu

RUN apt-get update

ARG AWS_REGION
RUN test -n "$AWS_REGION" || (echo "AWS_REGION  not set" && false)

ARG AWS_BUCKET_NAME
RUN test -n "$AWS_BUCKET_NAME" || (echo "AWS_BUCKET_NAME not set" && false)

ARG AWS_URL_DURATION_IN_SECONDS
RUN test -n "$AWS_URL_DURATION_IN_SECONDS" || (echo "AWS_URL_DURATION_IN_SECONDS  not set" && false)

ENV AWS_REGION = ${AWS_REGION}
ENV AWS_BUCKET_NAME = ${AWS_BUCKET_NAME}
ENV AWS_URL_DURATION_IN_SECONDS = ${AWS_URL_DURATION_IN_SECONDS}

FROM maven:3.8-amazoncorretto-18 AS build
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

FROM maven:3.8-amazoncorretto-18 AS test
WORKDIR /app
COPY . .
RUN mvn test

FROM amazoncorretto:18-alpine AS runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]