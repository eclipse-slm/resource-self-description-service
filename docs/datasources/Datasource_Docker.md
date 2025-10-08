# Data Source | Docker

Provides information about the Docker environment (e.g., Docker version, containers, images, volumes, networks, ...). To gather the data the Docker host must be
accessible. It provides the following additional configuration options:

| Environment Variable                  | Description                                                                                         | Required | Default Value        |
|---------------------------------------|-----------------------------------------------------------------------------------------------------|----------|----------------------|
| `DATASOURCES_DOCKER_DOCKERHOST`       | The URL of the Docker host (e.g., `unix:///var/run/docker.sock` or `tcp://localhost:2375`).         | Yes      | tcp://localhost:2375 |

It provides the following data source value definitions which can be used for template definition:

| Value Name               | Description                                                                                         | Semantic ID  |
|--------------------------|-----------------------------------------------------------------------------------------------------|--------------|
| `DockerVersion`          | The version of the Docker engine.                                                                   | -            |