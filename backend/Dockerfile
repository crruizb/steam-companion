FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080
CMD ["java", "-Xmx128m", "-jar", "app.jar"]