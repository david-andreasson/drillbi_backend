FROM amazoncorretto:21-alpine as builder

WORKDIR /app
COPY . .
RUN apk add --no-cache maven tesseract-ocr tesseract-ocr-swe tesseract-ocr-eng
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app
RUN apk add --no-cache tesseract-ocr tesseract-ocr-swe tesseract-ocr-eng
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/BACKEND_VERSION.txt BACKEND_VERSION.txt
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]