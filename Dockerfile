# Use a lightweight Java image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR
COPY camel-odoo-integration/target/app.jar /app/app.jar

# Expose Railway’s dynamic port
EXPOSE 8080

# Set entry point
CMD ["java", "-jar", "app.jar"]
