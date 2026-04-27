FROM node:20-alpine AS frontend-builder

WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /project

COPY backend/pom.xml .

RUN mkdir -p src/main/resources/static/new
COPY --from=frontend-builder /frontend/build/client/ src/main/resources/static/new/

COPY backend/src ./src

RUN chmod +x ./src/main/resources/generate-keystore.sh && \
    ./src/main/resources/generate-keystore.sh

RUN mvn -B clean -Pmysql -Dspring.profiles.active=mysql,https package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /usr/src/app/

COPY --from=builder /project/target/*.jar app.jar

COPY --from=builder /project/src/main/resources/keystore.p12 ./keystore.p12

EXPOSE 8443

ENV SERVER_PORT=8443
ENV SPRING_PROFILES_ACTIVE=mysql,https

CMD ["java", "-jar", "app.jar"]