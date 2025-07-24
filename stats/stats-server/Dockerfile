FROM eclipse-temurin:21-jdk-jammy as builder

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
RUN mkdir -p stats/stats-server

COPY pom.xml .
COPY stats/pom.xml ./stats/
COPY stats/stats-server/pom.xml ./stats/stats-server/

RUN mvn install -N -DskipTests && \
    mvn -f stats/pom.xml install -N -DskipTests

COPY stats/stats-server/src ./stats/stats-server/src

RUN mvn -f stats/stats-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
COPY --from=builder /app/stats/stats-server/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]