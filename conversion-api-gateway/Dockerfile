FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,id=conversion-gateway-maven,target=/root/.m2 mvn -B -ntp package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/application.jar .
EXPOSE 8080
USER 10001
ENTRYPOINT ["java", "-jar", "application.jar"]
