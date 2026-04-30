# Stage 1: Build
FROM gradle:8.11-jdk21 AS builder
WORKDIR /build

# Copy gradle config first for better layer caching
COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY build-logic build-logic

# Copy all modules
COPY golem-xiv-api golem-xiv-api
COPY golem-xiv-api-backend golem-xiv-api-backend
COPY golem-xiv-api-client golem-xiv-api-client
COPY golem-xiv-cli golem-xiv-cli
COPY golem-xiv-cognizer-anthropic golem-xiv-cognizer-anthropic
COPY golem-xiv-cognizer-dashscope golem-xiv-cognizer-dashscope
COPY golem-xiv-core golem-xiv-core
COPY golem-xiv-dom-export golem-xiv-dom-export
COPY golem-xiv-json golem-xiv-json
COPY golem-xiv-kotlin-metadata golem-xiv-kotlin-metadata
COPY golem-xiv-logging golem-xiv-logging
COPY golem-xiv-neo4j golem-xiv-neo4j
COPY golem-xiv-neo4j-starter golem-xiv-neo4j-starter
COPY golem-xiv-playwright golem-xiv-playwright
COPY golem-xiv-presenter golem-xiv-presenter
COPY golem-xiv-server golem-xiv-server
COPY golem-xiv-web golem-xiv-web
COPY api api

# Copy config files
COPY application.yaml application-deployment.yaml ./

# Build the fat JAR
RUN ./gradlew :golem-xiv-server:shadowJar --no-daemon --parallel

# Stage 2: Runtime
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

COPY --from=builder /build/golem-xiv-server/build/libs/golem-xiv-server-*-all.jar app.jar
COPY --from=builder /build/application-deployment.yaml application.yaml

ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
EXPOSE 8080

CMD ["--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
