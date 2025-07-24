FROM eclipse-temurin:21-jdk-jammy as builder

COPY . /app
WORKDIR /app

RUN mvn -f pom.xml clean install -N -DskipTests

RUN mvn -f stats/stats-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
COPY --from=builder /app/stats/stats-server/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]