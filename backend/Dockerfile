# ==============================================================================
# LifeLink Backend - Multi-stage Production Dockerfile
# ==============================================================================

# Stage 1: Build JAR using Maven with Eclipse Temurin JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build production executable JAR (skip tests during container packaging)
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal hardened runtime image using Eclipse Temurin JRE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-privileged user for container security
RUN groupadd -r lifelink && useradd -r -g lifelink -d /app lifelink

# Copy packaged JAR artifact
COPY --from=build /workspace/target/lifelink-backend-*.jar app.jar
RUN chown -R lifelink:lifelink /app

USER lifelink

ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
