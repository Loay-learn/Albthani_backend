# المرحلة 1: بناء الملف باستخدام Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# المرحلة 2: التشغيل باستخدام Java 21 (الخفيفة)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# إعداد المنفذ لـ Render
EXPOSE 8080

# تشغيل التطبيق
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT:8080}"]