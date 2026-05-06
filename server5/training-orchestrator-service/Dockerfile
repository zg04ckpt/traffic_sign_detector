# Build from server5 directory:
#   docker build -f training-orchestrator-service/Dockerfile -t server5/training-orchestrator-service:local .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY common-model/pom.xml common-model/pom.xml
COPY common-model/src common-model/src
RUN cd common-model && mvn -q install -DskipTests

COPY training-orchestrator-service/pom.xml training-orchestrator-service/pom.xml
COPY training-orchestrator-service/src training-orchestrator-service/src
RUN cd training-orchestrator-service && mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/training-orchestrator-service/target/training-orchestrator-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
