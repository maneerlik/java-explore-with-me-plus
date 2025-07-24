FROM eclipse-temurin:21-jdk-jammy as builder

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .
COPY stats/pom.xml ./stats/

RUN mvn install -N -DskipTests && \
    mvn -f stats/pom.xml install -N -DskipTests

COPY stats/stats-server ./stats/stats-server

RUN mvn -f stats/stats-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
COPY --from=builder /app/stats/stats-server/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]