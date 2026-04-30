# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
# Try to download dependencies to cache them (can speed up subsequent builds)
RUN mvn dependency:go-offline -B

# Copy the source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Set an environment variable for the port Render will map
ENV PORT=8080

# The base app uploads directory volume requirement
RUN mkdir -p /app/uploads

# Copy the JAR from the build stage
COPY --from=build /app/target/swasthyasetu-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
