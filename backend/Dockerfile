# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy the bundled Maven wrapper so we don't need mvn installed
COPY apache-maven-3.9.6 ./apache-maven-3.9.6
COPY pom.xml .
COPY src ./src

RUN chmod +x ./apache-maven-3.9.6/bin/mvn && \
    ./apache-maven-3.9.6/bin/mvn package -DskipTests --no-transfer-progress

# Stage 2: Slim runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
