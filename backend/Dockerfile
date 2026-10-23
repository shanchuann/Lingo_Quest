# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 先只拷 pom,依赖层可被 Docker 缓存
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline || true

COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/*.jar /app/app.jar

USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
