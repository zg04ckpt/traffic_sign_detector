# Build from server5 directory:
#   docker build -f discovery-server/Dockerfile -t server5/discovery-server:local .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY discovery-server/pom.xml discovery-server/pom.xml
COPY discovery-server/src discovery-server/src
RUN cd discovery-server && mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/discovery-server/target/discovery-server-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
