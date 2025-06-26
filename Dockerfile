# Start with a lightweight base image with Java
FROM eclipse-temurin:17-jre-alpine

# Copy the jar file into the container
COPY app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8081)
EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]