FROM openjdk:17-jdk-slim

WORKDIR /app

# Use the correct jar name here
COPY camel-odoo-integration/target/camel-odoo-integration-1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
