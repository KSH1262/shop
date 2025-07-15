# Dockerfile

FROM eclipse-temurin:17-jdk-focal AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# Stage 2: 최종 실행 이미지 생성 (JRE만 필요)
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=build /app/build/libs/shop-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]