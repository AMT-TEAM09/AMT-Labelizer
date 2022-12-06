# syntax = docker/dockerfile:1.2
FROM ubuntu

#TODO le truc des secret la
RUN apt-get update

ARG AWS_REGION
RUN test -n "$AWS_REGION" || (echo "AWS_REGION  not set" && false)

ENV AWS_REGION = ${AWS_REGION}

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