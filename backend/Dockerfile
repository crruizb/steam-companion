FROM openjdk:25-jdk-slim
WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080
CMD ["java", "-Xmx128m", "-jar", "app.jar"]