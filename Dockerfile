# 빌드 단계
FROM gradle:7.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

#실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
ARG JAR_FILE=api/build/libs/api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]