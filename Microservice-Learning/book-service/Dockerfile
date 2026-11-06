# Small image that has only Java 21 runtime (no JDK/Maven needed)
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the jar that Maven built into the image
COPY target/*.jar app.jar

# Document which port the app listens on
EXPOSE 8081

# Command that runs when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
