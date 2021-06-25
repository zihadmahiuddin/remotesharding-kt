FROM gradle:6.8.1-jdk11 AS builder

WORKDIR /app
COPY . /app
RUN gradle build

FROM openjdk:11-buster
WORKDIR /app
COPY --from=builder /app/examples/build/libs/*-client*.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
