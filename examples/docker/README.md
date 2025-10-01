# Docker

This directory contains an example how to deploy the self-description-service using Docker.

Update the property `DEPLOYMENT_HOSTNAME` in the `.env` file to the hostname or IP address of your Docker host. Alternatively, you can add an entry to the
hosts file on your host system to map the hostname `slm.local` to the IP address of your Docker host.

Execute the following command in this directory to start the docker compose stack:
```
docker compose up -d
```

This will start the self-description service as well as a [Eclipse BaSyx minimal setup](https://github.com/eclipse-basyx/basyx-java-server-sdk/blob/main/examples/BaSyxMinimal).
The resulting AAS and submodels can be viewed using the [Eclipse BaSyx AAS Web UI](https://github.com/eclipse-basyx/basyx-aas-web-ui) => 
http://slm.local:33000
