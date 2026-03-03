# 1. Сборка с Maven + JDK 17
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve
COPY src ./src
RUN mvn package

# 2. Минимальный образ для запуска
FROM eclipse-temurin:17-jdk-jammy

# Устанавливаем OpenCV native libraries
RUN apt-get update && apt-get install -y libopencv-dev && rm -rf /var/lib/apt/lists/*

# Копируем jar
COPY --from=build /app/target/fractalimagecompression-1.0-SNAPSHOT.jar /app/app.jar

# Папка для картинок
WORKDIR /app
VOLUME /app/images

# Настраиваем LD_LIBRARY_PATH для OpenCV
ENV LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu

CMD ["java", "-jar", "app.jar"]
