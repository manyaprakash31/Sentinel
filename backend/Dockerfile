# Stage 1: Build the application using Maven and Temurin JDK 17
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Render supplies $PORT dynamically; fallback to 8085 if not set
ENV PORT=8085
EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8085} -jar app.jar"]
