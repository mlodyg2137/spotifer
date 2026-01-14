# In development, you might use a simpler Dockerfile like this (when you change code in src/, you need to rebuild the image by command:
# mvn -DskipTests package
#
#FROM eclipse-temurin:21-jdk-alpine
#
#WORKDIR /app
#
#COPY target/*.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]



# ====== STAGE 1: build ======
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -DskipTests -q dependency:go-offline

COPY src ./src
RUN mvn -DskipTests package

# ====== STAGE 2: runtime ======
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
