#FROM maven:3.9.9-eclipse-temurin-21 AS builder
#
#WORKDIR /build
#
#COPY pom.xml .
#RUN mvn -B dependency:go-offline
#
#COPY src ./src
#RUN mvn -B clean package -DskipTests
#
#FROM eclipse-temurin:21-jre-alpine
#
#WORKDIR /app
#
#COPY --from=builder /build/target/*.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java","-jar","/app/app.jar"]


# ========= BUILD STAGE =========
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ========= RUNTIME STAGE =========
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN mkdir -p /app/storage/pdf_report/reports && chmod -R 777 /app/storage

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]