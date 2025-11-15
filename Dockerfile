FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY golem-xiv-server/build/libs/golem-xiv-server-all.jar app.jar
COPY application-deployment.yaml application.yaml
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
EXPOSE 8080
CMD ["--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
