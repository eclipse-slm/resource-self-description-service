FROM eclipse-temurin:21
MAINTAINER Matthias Schneider (matthias.schneider@ipa.fraunhofer.de)

ENV CONSUL_SCHEME="http" \
    CONSUL_HOST="consul" \
    CONSUL_PORT=8500 \
    CONSUL_DATACENTER="fabos" \
    CONSUL_ACLTOKEN="password" \
    AAS_BROKER="aas-broker" \
    AAS_AASREGISTRY_HOST="aas-host" \
    AAS_AASREPOSITORY_HOST="aas-host" \
    AAS_AASSUBMODELREGISTRY_HOST="aas-host" \
    AAS_AASSUBMODELREPOSITORY_HOST="aas-host"

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app

VOLUME "/app/keycloak"

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
#ENTRYPOINT ["/bin/bash", "-c", "top"]
