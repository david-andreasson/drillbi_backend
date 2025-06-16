FROM amazoncorretto:21-alpine AS builder

WORKDIR /app
COPY . .
RUN apk add --no-cache maven tesseract-ocr curl \
  && mkdir -p /usr/share/tessdata \
  && curl -L -o /usr/share/tessdata/swe.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/swe.traineddata \
  && curl -L -o /usr/share/tessdata/eng.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app
RUN apk add --no-cache tesseract-ocr curl \
  && mkdir -p /usr/share/tessdata \
  && curl -L -o /usr/share/tessdata/swe.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/swe.traineddata \
  && curl -L -o /usr/share/tessdata/eng.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/BACKEND_VERSION.txt BACKEND_VERSION.txt
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]