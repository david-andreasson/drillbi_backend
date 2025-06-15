# Byggsteg
# Byggsteg
FROM maven:3.9.4-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
RUN ls -lh /app/target

# Runtime-steg med Tesseract och Java
FROM openjdk:21-jdk-alpine
WORKDIR /app
# Installera Tesseract och dess beroenden för Alpine
RUN apk add --no-cache tesseract tesseract-ocr-data swe-dev
RUN java -version
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/BACKEND_VERSION.txt BACKEND_VERSION.txt
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
