FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src/ src/
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre AS final
WORKDIR /app
EXPOSE 8083
COPY ca-certs.pem /usr/local/share/ca-certificates/ca-certs.crt
RUN apt-get update && apt-get install -y ca-certificates \
    && update-ca-certificates \
    && keytool -import -trustcacerts \
       -keystore /opt/java/openjdk/lib/security/cacerts \
       -storepass changeit -noprompt \
       -alias corporate-ca \
       -file /usr/local/share/ca-certificates/ca-certs.crt
COPY --from=build /src/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]