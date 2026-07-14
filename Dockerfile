# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the JAR package
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/weather-mcp-server-1.0.0.jar weather-mcp-server.jar

# Define environment variables
ENV OPENWEATHER_API_KEY=""

# Run the MCP server
ENTRYPOINT ["java", "-jar", "weather-mcp-server.jar"]
