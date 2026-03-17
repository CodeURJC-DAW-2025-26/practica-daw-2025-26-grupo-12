FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /project

COPY backend/pom.xml .
COPY backend/src ./src

RUN chmod +x ./src/main/resources/generate-keystore.sh && \
    ./src/main/resources/generate-keystore.sh

RUN mvn -B clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /usr/src/app/

COPY --from=builder /project/target/*.jar app.jar

COPY --from=builder /project/src/main/resources/keystore.p12 keystore.p12

EXPOSE 8443

ENV SERVER_PORT=8443

CMD ["java", "-jar", "app.jar"]