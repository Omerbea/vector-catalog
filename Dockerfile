FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the fat jar built by Maven
COPY target/vector-catalog-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
