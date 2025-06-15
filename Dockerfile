# Byggsteg
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime-steg med Tesseract
FROM eclipse-temurin:21-jre
WORKDIR /app
# Installera Tesseract och dess beroenden
RUN apt-get update && apt-get install -y tesseract-ocr libtesseract-dev && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/BACKEND_VERSION.txt BACKEND_VERSION.txt
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
