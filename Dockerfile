FROM java:8-jre-alpine


RUN mkdir -p /opt/vertx

COPY build/libs/postgres-services-1.0-SNAPSHOT-fat.jar /opt/vertx/

COPY bin/config.json /opt/vertx

CMD ["java", "-jar", "/opt/vertx/postgres-services-1.0-SNAPSHOT-fat.jar", "-conf", "/opt/vertx/config.json","-cluster"]