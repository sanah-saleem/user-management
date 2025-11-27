# 1) Build stage (uses Maven image)
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and resolve deps (better caching)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -q clean package -DskipTests

# 2) Run stage (lightweight JDK)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose app port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
