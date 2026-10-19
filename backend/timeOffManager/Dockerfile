# Multi-stage Dockerfile for Spring Boot Time Off Manager (Java 25)

# Stage 1: Build JAR using Maven
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

# Install Maven
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

# Copy POM and pre-fetch dependencies for Docker layer caching
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copy sources and build release JAR
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:25-jre
WORKDIR /app

# Install curl for Actuator health checks and create non-root user
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    addgroup --system appgroup && \
    adduser --system --ingroup appgroup appuser

COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser:appgroup

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    TZ=Europe/Bucharest

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Duser.timezone=Europe/Bucharest", "-jar", "app.jar"]
