# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Build the application
COPY src ./src
RUN mvn -q package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create a non-root user and a writable logs directory
RUN groupadd --system app && useradd --system --gid app --home /app app \
    && mkdir -p /app/logs \
    && chown -R app:app /app

# Copy the built jar
COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

EXPOSE 8080

# Keep memory within Render's small instances
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]