# syntax = docker/dockerfile:1.2
FROM ubuntu

RUN apt-get update

FROM maven:3.8-amazoncorretto-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM maven:3.8-amazoncorretto-17 AS test
WORKDIR /app
COPY . .
RUN mvn clean test

FROM eclipse-temurin:17 AS runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","--enable-preview","-jar","app.jar"]