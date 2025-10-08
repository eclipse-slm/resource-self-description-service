# Resource Self-Description Service

[![Build Status](https://github.com/eclipse-slm/resource-self-description-service/actions/workflows/build.yml/badge.svg)](https://github.com/eclipse-slm/resource-self-description-service/actions)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://img.shields.io/badge/License-Apache_2.0-blue.svg)

The **Resource Self-Description Service** provides standardized, machine-readable descriptions of devices using the **Asset Administration Shell (AAS)**. It 
enables devices in manufacturing environments to expose self-descriptions of static and dynamic information, supporting interoperability, integration, and 
lifecycle management of heterogeneous system landscapes within manufacturing environments. Data of the device can be accessed using different data sources. 
A data source can provide default submodels, or the data variables of a data source can be used in parametrized submodel templates defined in AASX format.
These parameters inside the submodel template are resolved with the current value from the data source when the submodel gets queried.

## Motivation
Today's manufacturing environments are characterized by a growing number of devices, ranging from traditional industrial controllers to modern edge devices and 
IoT sensors from different vendors. This leads to highly heterogeneous system landscapes in manufacturing environments, posing significant challenges for
managing these system landscapes. While static information about devices (e.g., manufacturer, model, technical specification) is often provided by the
manufacturer, dynamic information (e.g., state values, software versions, configuration parameters) is often difficult to access or not available in a
standardized way. To address this issue, the **Resource Self-Description Service** was developed to provide such information in a standardized way using the 
Asset Administration Shell (AAS).

## Features
- Access device data from various data sources (e.g., files, operating system, system and hardware information, container environments, ...)
- Use default submodels of data sources or define parametrized submodel templates in AASX format depending on your use case
- [Submodel Repository API](https://app.swaggerhub.com/apis/Plattform_i40/SubmodelRepositoryServiceSpecification/V3.0.1_SSP-004) to provide submodels
- Deployment as JAR or Docker container
- Secure access to the API using OAuth2 and OpenID Connect (OIDC) (e.g., Keycloak)
- Encrypt API using TLS

---

## Getting Started

The service can be run as a standalone Java application or as a Docker container. See the examples in the [examples](examples) directory for the different deployment
options.

## API
The service exposes a REST API based on the [Submodel Repository Service Specification](https://app.swaggerhub.com/apis/Plattform_i40/SubmodelRepositoryServiceSpecification/V3.0.1_SSP-004)
to provide the submodels. By default, the Swagger UI API documentation is available at `http://<device-ip-or-hostname>:48080/swagger-ui.html`.

## Configuration

Configuration is primarily done via the [application.yml](app/src/main/resources/application.yml). As Spring Boot is used as a framework, all values in
[application.yml](app/src/main/resources/application.yml) can also be set via environment variables (see [Spring Boot Docs: Externalized Configuration Using 
Environment Variables](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.env-variables). The following 
table provides an overview of the most important configuration options as environment variables:

| Environment Variable       | Description                                                                                                                                                                                                                                                                       | Required | Default Value                                 |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|-----------------------------------------------|
| `RESOURCE_ID`              | Id of the resource for which the submodels are provided                                                                                                                                                                                                                           | Yes      | 00000000-0000-0000-0000-000000000000          |
| `RESOURCE_AAS_PREFIX`      | Prefix for the AAS id of the resource AAS                                                                                                                                                                                                                                         | No       | Resource                                      |
| `RESOURCE_AAS_ID`          | Id of the resource AAS. By default it is created using the `RESOURCE_AAS_PREFIX` and the `RESOURCE_ID`                                                                                                                                                                            | No       | Resource_00000000-0000-0000-0000-000000000000 |
| `DEPLOYMENT_SCHEME`        | The HTTP scheme with which the service can be accessed.                                                                                                                                                                                                                           | No       | http                                          |
| `DEPLOYMENT_HOST`          | The host (IP or hostname) on which the service can be accessed.                                                                                                                                                                                                                   | No       | localhost                                     |
| `DEPLOYMENT_PORT`          | The port on which the service can be accessed.                                                                                                                                                                                                                                    | No       | 48080                                         |
| `DEPLOYMENT_PATH`          | The base path under which the service can be accessed.                                                                                                                                                                                                                            | No       | /                                             |
| `DEPLOYMENT_URL`           | The full URL (scheme, host, port, path) under which the service can be accessed. By default it is created using `DEPLOYMENT_SCHEME`, `DEPLOYMENT_HOSTNAME`, `DEPLOYMENT_PORT` and `DEPLOYMENT_PATH`. The value is used as endpoint for the registration at the Submodel Registry. | No       | http://localhost:48080/                       |
| `SECURITY_ENABLED`         | Whether security is enabled or not. If enabled, OAuth2 / OpenID Connect (OIDC) is used for authentication and authorization as well as TLS to encrypt API access.                                                                                                                 | No       | false                                         |
| `SECURITY_ORIGINS`         | Comma-separated list of allowed CORS origins. Only used if `SECURITY_ENABLED` is set to `true`.                                                                                                                                                                                   | No       | *                                             |
| `SECURITY_CERTIFICATE`     | Path to the TLS certificate file (in PEM format). Only used if `SECURITY_ENABLED` is set to `true`.                                                                                                                                                                               | No       | classpath:./certs/resource.crt                |
| `SECURITY_PRIVATE_KEY`     | Path to the TLS private key file (in PEM format). Only used if `SECURITY_ENABLED` is set to `true`.                                                                                                                                                                               | No       | classpath:./certs/resource.key                |
| `JWT_AUTH_ISSUER_URI`      | The issuer URI of the OpenID Connect (OIDC) provider (e.g., Keycloak). Only used if `SECURITY_ENABLED` is set to `true`.                                                                                                                                                          | No       | issuer-uri: https://localhost/auth/realms/slm |
| `SERVER_PORT`              | The port on which the service listens for incoming requests.                                                                                                                                                                                                                      | No       | 48080                                         |
| `AAS_AASREGISTRY_URL`      | The URL of the Shell Registry to lookup the resource AAS. The AAS must exist, it is not created automatically. If the AAS is missing, application startup will fail.                                                                                                              | Yes      | http://localhost:8082                         |
| `AAS_SUBMODELREGISTRY_URL` | The URL of the Submodel Registry where the submodels are registered. For the registration the `DEPLOYMENT_URL` is used for the endpoint definition in the submodel descriptor.                                                                                                    | Yes      | http://localhost:8083                         |
| `DATASOURCES_...`          | Depending on the data source, different configuration options are available. See the documentation for the specific data source for the available configuration options.                                                                                                          | No       | -                                             |

---


## Data Sources
The service supports different data sources to access the device data. The documentation for the data sources can be found [here](docs/datasources).