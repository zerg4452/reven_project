FROM gradle:8.14.3-jdk21 AS build
WORKDIR /workspace
COPY settings.gradle build.gradle ./
COPY src ./src
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul
COPY --from=build /workspace/build/libs/*.jar app.jar
COPY legacy-php-source/croll ./legacy-php-source/croll
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
