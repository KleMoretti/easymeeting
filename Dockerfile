FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:8-jre
WORKDIR /opt/easymeeting
COPY --from=build /workspace/target/easymeeting-1.0.jar /opt/easymeeting/easymeeting.jar
RUN mkdir -p /opt/easymeeting/file /opt/easymeeting/logs
EXPOSE 6060 6061
ENTRYPOINT ["java","-jar","/opt/easymeeting/easymeeting.jar"]
