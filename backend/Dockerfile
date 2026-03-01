# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Install Maven
RUN apk add --no-cache maven 2>/dev/null || true

# Configure Aliyun Maven mirror for faster downloads in China
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\
<settings><mirrors><mirror>\
<id>aliyun</id>\
<url>https://maven.aliyun.com/repository/public</url>\
<mirrorOf>central</mirrorOf>\
</mirror></mirrors></settings>' > /root/.m2/settings.xml

COPY pom.xml .
# Download dependencies first (cache layer)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -f pom.xml dependency:go-offline -q

COPY src ./src

RUN mvn -f pom.xml clean package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# su-exec for dropping privileges (lightweight alternative to gosu)
RUN apk add --no-cache su-exec

# Copy the fat jar
COPY --from=build /workspace/target/*.jar app.jar

# Directories for uploads and SQLite DB
RUN mkdir -p /app/uploads /data && \
    chown -R appuser:appgroup /app /data

# Entrypoint script fixes volume permissions then drops to appuser
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]
