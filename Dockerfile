FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# give permission to mvnw
RUN chmod +x mvnw

# build project
RUN ./mvnw clean package -DskipTests

CMD ["java","-jar","target/identity-reconciliation-0.0.1-SNAPSHOT.jar"]