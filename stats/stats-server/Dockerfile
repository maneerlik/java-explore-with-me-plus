FROM eclipse-temurin:21-jre-jammy

# Временная папка для работы Java
VOLUME /tmp

# Копируем JAR-файл
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Запуск с возможностью дебага
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]