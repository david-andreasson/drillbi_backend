# Byggsteg
# Byggsteg
FROM maven:3.9.4-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
RUN ls -lh /app/target

# Runtime-steg med Tesseract och Java
FROM eclipse-temurin:21-jre
WORKDIR /app
# Installera Tesseract och dess beroenden
RUN apt-get update && apt-get install -y tesseract-ocr libtesseract-dev && rm -rf /var/lib/apt/lists/*
RUN java -version
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/BACKEND_VERSION.txt BACKEND_VERSION.txt
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
