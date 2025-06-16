FROM eclipse-temurin:21-jdk as builder

WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/BACKEND_VERSION.txt BACKEND_VERSION.txt
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
