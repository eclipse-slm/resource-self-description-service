FROM eclipse-temurin:21
MAINTAINER Matthias Schneider (matthias.schneider@ipa.fraunhofer.de)

ENV AAS_AASREGISTRY_HOST="aas-registry" \
    AAS_AASREPOSITORY_HOST="aas-repository" \
    AAS_AASSUBMODELREGISTRY_HOST="submodel-registry" \
    RESOURCE_ID="00000000-0000-0000-0000-000000000000" \
    USECONFIGURLS=true

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app

VOLUME "/app/keycloak"

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
