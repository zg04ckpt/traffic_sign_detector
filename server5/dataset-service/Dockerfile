# Build from server5 directory:
#   docker build -f dataset-service/Dockerfile -t server5/dataset-service:local .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY common-model/pom.xml common-model/pom.xml
COPY common-model/src common-model/src
RUN cd common-model && mvn -q install -DskipTests

COPY dataset-service/pom.xml dataset-service/pom.xml
COPY dataset-service/src dataset-service/src
RUN cd dataset-service && mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/dataset-service/target/dataset-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
