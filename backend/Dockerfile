FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY . .
RUN chmod +x mvnw && ./mvnw -B -DskipTests package \
    && find target -maxdepth 1 -name 'arabic-reference-*.jar' ! -name '*.original' -exec cp {} /tmp/app.jar \;

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --uid 10001 app
COPY --from=build /tmp/app.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
