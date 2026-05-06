# Build from server5 directory:
#   docker build -f aimodel-service/Dockerfile -t server5/aimodel-service:local .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY common-model/pom.xml common-model/pom.xml
COPY common-model/src common-model/src
RUN cd common-model && mvn -q install -DskipTests

COPY aimodel-service/pom.xml aimodel-service/pom.xml
COPY aimodel-service/src aimodel-service/src
RUN cd aimodel-service && mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/aimodel-service/target/aimodel-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
