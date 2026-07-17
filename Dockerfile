# Use a lightweight Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from the target directory
# (Maven outputs builds to the target/ folder)
COPY target/*.jar app.jar

# Expose port 8080 (Cloud Run's default port)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]