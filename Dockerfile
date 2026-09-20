# ---------- BUILD STAGE ----------
FROM maven:3.9-eclipse-temurin-23 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# ---------- RUN STAGE ----------
FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=build /app/target/*.jar TriageIQ.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "TriageIQ.jar"]