FROM eclipse-temurin:17-jdk-alpine

ENV APP_HOME=/opt/app
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME

COPY ./acl-service-0.0.1-SNAPSHOT.jar api.jar

EXPOSE 8080

ENTRYPOINT java -jar api.jar