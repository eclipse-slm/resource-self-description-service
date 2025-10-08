# Data Source | System Info

Provides system and hardware information (e.g., CPU, memory, disk, network, operating system, ...) using the [OPERATING SYSTEM & HARDWARE INFORMATION (OSHI) library](https://github.com/oshi/oshi).
It provides the following additional configuration options:

| Environment Variable                                       | Description                                                                                                 | Required | Default Value |
|------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|----------|---------------|
| `DATASOURCES_SYSTEMINFO_CACHING_OVERALL_ENABLED`           | Whether overall caching is enabled or not. If enabled, the overall system information is cached.            | No       | false         |
| `DATASOURCES_SYSTEMINFO_CACHING_OVERALL_REFRESHINTERVALLS` | The refresh intervall in seconds for the overall caching.                                                   | No       | 300           |
| `DATASOURCES_SYSTEMINFO_CACHING_CPU_ENABLED`               | Whether CPU caching is enabled or not. If enabled, the CPU information is cached.                           | No       | false         |  
| `DATASOURCES_SYSTEMINFO_CACHING_CPU_REFRESHINTERVALLS`     | The refresh intervall in seconds for the CPU caching.                                                       | No       | 60            |
| `DATASOURCES_SYSTEMINFO_CACHING_MEMORY_ENABLED`            | Whether memory caching is enabled or not. If enabled, the memory information is cached.                     | No       | false         |  
| `DATASOURCES_SYSTEMINFO_CACHING_MEMORY_REFRESHINTERVALLS`  | The refresh intervall in seconds for the memory caching.                                                    | No       | 60            |
| `DATASOURCES_SYSTEMINFO_CACHING_OS_ENABLED`                | Whether operating system caching is enabled or not. If enabled, the operating system information is cached. | No       | false         |
| `DATASOURCES_SYSTEMINFO_CACHING_OS_REFRESHINTERVALLS`      | The refresh intervall in seconds for the operating system caching.                                          | No       | 300           |

It provides the following data source value definitions which can be used for template definition:

| Value Name           | Description                                        | Semantic ID                                         |
|----------------------|----------------------------------------------------|-----------------------------------------------------|
| `cpu.architecture`   | The architecture of the CPU (e.g., x86_64, arm64). | http://eclipse.dev/slm/aas/sme/SysteInfo/CPU/Arch   |
| `cpu.name`           | The name of the CPU.                               | -                                                   |
| `cpu.vendor`         | The vendor of the CPU.                             | -                                                   |
| `cpu.logical_cores`  | Number of logical CPU cores.                       | -                                                   |
| `cpu.physical_cores` | Number of physical CPU cores.                      | -                                                   |
| `cpu.max_frequency`  | Maximum CPU frequency in Hz.                       | -                                                   |
| `mem.free_memory`    | Free memory in bytes.                              | -                                                   |
| `mem.used_memory`    | Used memory in bytes.                              | -                                                   |
| `mem.total_memory`   | Total memory in bytes.                             | -                                                   |
| `os.version`         | Operating system version.                          | http://eclipse.dev/slm/aas/sme/SysteInfo/OS/Version |
| `os.build_number`    | Operating system build number.                     | -                                                   |
| `os.bitness`         | Operating system bitness (e.g., 64, 32).           | -                                                   |
| `os.boottime`        | System boot time (timestamp).                      | -                                                   |
| `os.family`          | Operating system family.                           | -                                                   |
| `os.uptime_seconds`  | System uptime in seconds.                          | -                                                   |
| `os.manufacturer`    | Operating system manufacturer.                     | -                                                   |

In addition, the data source provides a FreeMarker method for accessing all information provided by [OSHI]((https://github.com/oshi/oshi)). The information
provided by OSHI is parsed as JSON and can be read using a JSONPath expression. Depending on the system, querying all operating system and hardware information
can take a long time. The information is therefore cached, which can be configured using the `DATASOURCES_SYSTEMINFO_CACHING_OVERALL_REFRESHINTERVALLS`
configuration parameter described above.

Example:
```
${SystemInfo("$.hardware.computerSystem.firmware.version")}
```