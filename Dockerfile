# Use official OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy the JAR file from the target directory (relative path from the repository root)
COPY camel-odoo-integration/target/camel-odoo-integration-1.0-SNAPSHOT.jar /app/app.jar

# Expose the port your app runs on (8080 is a common default for web apps)
EXPOSE 8080

# Run the app using Java
CMD ["java", "-jar", "app.jar"]
