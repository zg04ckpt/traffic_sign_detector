# Build from server5 directory:
#   docker build -f config-server/Dockerfile -t server5/config-server:local .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY config-server/pom.xml config-server/pom.xml
COPY config-server/src config-server/src
RUN cd config-server && mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/config-server/target/config-server-*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
