# syntax = docker/dockerfile:1.2
FROM eclipse-temurin:17-jdk-jammy AS base
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN ./mvnw dependency:resolve

FROM base AS build
RUN ./mvnw package -DskipTests

FROM base AS test
RUN ./mvnw test

FROM eclipse-temurin:17-jre-jammy AS production
EXPOSE 8081
RUN groupadd -r user && useradd -r -g user user
USER user:user
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
CMD ["java","--enable-preview",  "-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]