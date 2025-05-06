# Use an official Maven image (with Eclipse Temurin for OpenJDK 21) to build the app
FROM maven:3.9.4-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code into the container
COPY pom.xml .
COPY src ./src

# Build the application and package it into a jar file
RUN mvn clean package -DskipTests

# Use an official OpenJDK image to run the app
FROM eclipse-temurin:21-jre

# Copy the jar file from the builder stage using a wildcard
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
