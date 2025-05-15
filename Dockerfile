FROM amazoncorretto:21-alpine as builder

WORKDIR /app
COPY pom.xml ./
RUN apk add --no-cache maven && mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
