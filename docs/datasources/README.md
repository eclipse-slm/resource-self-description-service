## Data Sources

The service supports different data sources to access the device data. Currently, the following data sources are available (docs of datasource can be found
by clicking on the name of the data source):

- **[Template](Datasource_Docker.md)**: AASX submodel templates with parameters that are resolved using template variables or values from other data sources.
- **[SystemInfo](Datasource_SystemInfo.md)**: Provides system and hardware information (e.g., CPU, memory, disk, network, operating system, ...) using the [OSHI](https://github.com/oshi/oshi) library.
- **[Docker](Datasource_Docker.md)**: Provides information about the Docker environment (e.g., Docker version, containers, images, volumes, networks, ...) using the Docker host.

All data sources have the following common configuration options:

| Environment Variable                   | Description                                                      | Required | Default Value |
|----------------------------------------|------------------------------------------------------------------|----------|---------------|
| `DATASOURCES_<NAME>_ENABLED`           | Whether the data source is enabled or not.                       | No       | true          |
| `DATASOURCES_<NAME>_PROVIDESUBMODELS`  | Whether the data source provides its default submodel or not.    | No       | true          |
| `DATASOURCES_<NAME>_VALUEBYSEMANTICID` | Whether the data source provides values for specific ids or not. | No       | true          |

