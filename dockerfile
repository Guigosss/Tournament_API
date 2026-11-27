# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Poms d'abord : la couche ne change que si les dépendances changent
COPY pom.xml .
COPY dl/pom.xml dl/
COPY dal/pom.xml dal/
COPY bll/pom.xml bll/
COPY api/pom.xml api/

COPY dl/src dl/src
COPY dal/src dal/src
COPY bll/src bll/src
COPY api/src api/src

# Build du module api et des modules dont il dépend (dl, dal, bll).
# Le cache BuildKit évite de retélécharger les dépendances à chaque build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl api -am package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/api/target/api-*.jar app.jar
EXPOSE 8081 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
