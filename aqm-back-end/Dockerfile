FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/aqm-back-end-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
