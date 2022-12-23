# syntax = docker/dockerfile:1
FROM eclipse-temurin:17-jdk-jammy AS base
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw \
    # ^ replaces CRLF with LF
    && chmod +x mvnw \
    # ^ makes mvnw executable
    && ./mvnw dependency:resolve
COPY src ./src

FROM base AS build
RUN ./mvnw package -DskipTests

FROM base AS test
CMD ["./mvnw", "test"]

FROM eclipse-temurin:17-jre-jammy AS production
EXPOSE 8080
RUN groupadd -r spring \
    && useradd -r -g spring spring
USER spring:spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
CMD ["java","--enable-preview",  "-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]