# Build from server5 directory:
#   docker build -f api-gateway/Dockerfile -t server5/api-gateway:local .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src
RUN cd api-gateway && mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
