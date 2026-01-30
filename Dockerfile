# syntax=docker/dockerfile:1

FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY blog-api/pom.xml blog-api/
COPY blog-core/pom.xml blog-core/
COPY blog-service/pom.xml blog-service/
COPY blog-web/pom.xml blog-web/

RUN mvn -pl blog-web -am dependency:go-offline -B

COPY . .
RUN mvn -pl blog-web -am package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r blog && useradd -r -g blog blog

COPY --from=builder /app/blog-web/target/blog-web-*.jar /app/app.jar

RUN chown -R blog:blog /app
USER blog

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
