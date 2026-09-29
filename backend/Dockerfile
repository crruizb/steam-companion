FROM eclipse-temurin:25-jre-alpine

# Run as an unprivileged user instead of root
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app

COPY --chown=app:app build/libs/*.jar app.jar
USER app

# Size the heap from the container's memory limit instead of a fixed -Xmx.
# 50% leaves room for the ~150-200MB Spring Boot uses outside the heap
# (512Mi limit -> 256MB heap). Override with the JAVA_TOOL_OPTIONS environment variable.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50.0"

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
