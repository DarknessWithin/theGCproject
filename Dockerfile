# Use official OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy the JAR file into the image
COPY camel-odoo-integration/target/camel-odoo-integration-1.0-SNAPSHOT.jar /app/app.jar

# Expose a dynamic port (Railway sets PORT env)
EXPOSE 8080

# Run the app and pass the Railway-assigned port
CMD ["java", "-jar", "app.jar"]
