FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /workspace
COPY . .
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:21-jre-jammy

VOLUME /tmp
COPY --from=builder /workspace/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]