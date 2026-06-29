FROM gradle:9-jdk-25-and-25-alpine AS build
WORKDIR /app
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle src src
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
