# Use an official Maven image to build the app
FROM maven:3.8-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and the source code into the container
COPY pom.xml ./
COPY src ./src

# Build the application
RUN mvn clean install -DskipTests

# Use an OpenJDK image for the runtime
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/polling-app-server-*.jar app.jar

# Expose the port the app will run on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
