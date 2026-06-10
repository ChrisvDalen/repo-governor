# Build from the repository root: docker build -f repo-governor-server/Dockerfile .
FROM gradle:8.14-jdk21 AS build
WORKDIR /workspace
COPY settings.gradle build.gradle ./
COPY repo-governor-core repo-governor-core
COPY repo-governor-cli repo-governor-cli
COPY repo-governor-server repo-governor-server
COPY repo-governor-contracts repo-governor-contracts
RUN gradle :repo-governor-server:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/repo-governor-server/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
