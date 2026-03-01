# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY pom.xml .
# Download dependencies first (cache layer)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml dependency:go-offline -q 2>/dev/null || \
    (apk add --no-cache maven && mvn -f pom.xml dependency:go-offline -q)

# Install Maven if not present in base image
RUN apk add --no-cache maven 2>/dev/null || true

COPY src ./src

RUN mvn -f pom.xml clean package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the fat jar
COPY --from=build /workspace/target/*.jar app.jar

# Directories for uploads and SQLite DB
RUN mkdir -p /app/uploads /data && \
    chown -R appuser:appgroup /app /data

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
