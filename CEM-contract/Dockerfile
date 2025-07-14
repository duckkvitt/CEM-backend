# Multi-stage build for CEM Contract Service
FROM openjdk:17-jdk-slim as builder

# Install build dependencies
RUN apt-get update && apt-get install -y \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

# Install runtime dependencies for PDF processing and security
RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-dejavu-core \
    fonts-dejavu-extra \
    ca-certificates \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create application user
RUN groupadd -r cemapp && useradd -r -g cemapp -s /bin/false cemapp

# Create directories
RUN mkdir -p /app/logs /app/config /app/certs /etc/ssl /tmp/cem-signatures && \
    chown -R cemapp:cemapp /app && \
    chown cemapp:cemapp /tmp/cem-signatures

# Set working directory
WORKDIR /app

# Copy application JAR
COPY --from=builder /app/target/cem-contract-*.jar app.jar

# Copy configuration files
COPY --chown=cemapp:cemapp src/main/resources/application-prod.yml /app/config/

# Set up certificate directory
VOLUME ["/app/certs", "/app/logs", "/etc/ssl"]

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-server -Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"
ENV APP_OPTS="--spring.config.location=classpath:/application.yml,file:/app/config/application-prod.yml"

# JVM optimization for containers
ENV JAVA_OPTS="$JAVA_OPTS -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# Security and performance options
ENV JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8 -Duser.timezone=UTC"

# Digital signature specific options
ENV JAVA_OPTS="$JAVA_OPTS -Djava.awt.headless=true -Dcom.sun.management.jmxremote=false"

# Expose port
EXPOSE 8080 8443

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Switch to non-root user
USER cemapp

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar $APP_OPTS"]

# Labels for container metadata
LABEL maintainer="CEM Development Team"
LABEL version="1.0.0"
LABEL description="CEM Contract Service with Digital Signature Support"
LABEL org.opencontainers.image.title="CEM Contract Service"
LABEL org.opencontainers.image.description="Enterprise contract management with PAdES digital signatures"
LABEL org.opencontainers.image.vendor="G47 Solutions"
LABEL org.opencontainers.image.version="1.0.0" 