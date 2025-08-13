# Multi-stage build: Build stage
FROM amazoncorretto:17-alpine AS build

# Install necessary packages for building
RUN apk add --no-cache gcompat

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src/ ./src/

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM amazoncorretto:17-alpine AS runtime

WORKDIR /app

# Create a non-root user for security (Alpine style)
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Copy the built JAR from build stage
COPY --from=build /app/target/llm-vector-cache-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

# Use JVM options optimized for containers
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
