# Multi-stage Dockerfile for Liberty Mutual Insurance Platform services
# ADR-0002: Spring Boot 2.7.x with Java 11

FROM maven:3.9-eclipse-temurin-11 AS builder

WORKDIR /build
COPY pom.xml .
COPY customer-service/pom.xml customer-service/
COPY policy-service/pom.xml policy-service/
COPY audit-service/pom.xml audit-service/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

COPY . .
RUN mvn package -DskipTests -B

# --- customer-service ---
FROM eclipse-temurin:11-jre-alpine AS customer-service
WORKDIR /app
COPY --from=builder /build/customer-service/target/*.jar app.jar
EXPOSE 3001
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:3001/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]

# --- policy-service ---
FROM eclipse-temurin:11-jre-alpine AS policy-service
WORKDIR /app
COPY --from=builder /build/policy-service/target/*.jar app.jar
EXPOSE 3002
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:3002/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]

# --- audit-service ---
FROM eclipse-temurin:11-jre-alpine AS audit-service
WORKDIR /app
COPY --from=builder /build/audit-service/target/*.jar app.jar
EXPOSE 3003
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:3003/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
