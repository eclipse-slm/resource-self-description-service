# JAR

This directory contains an example how to deploy the self-description-service using a JAR file and to register it as a systemd service unit.

### Prerequisites:
- Java 21 or higher installed on your system
- Linux distribution with systemd (if you want to register the service as a systemd unit)

### Execution
Download the latest self-description-service JAR file from the [releases](https://github.com/eclipse-slm/resource-self-description-service/releases) page and 
place it in your desired installation directory (e.g. `/opt/resource-self-description-service/`). The self-description-service can be started using the command 
below. Make sure to replace the placeholders (e.g. `<<your-resource-id>>`) with your actual values.

```shell
java -jar <<your-jar-file-path>>  \
  --resource.id=<<your-resource-id>> \
  --server.port=<<your-port>> \
  --deployment.url=https://<<your-hostname>>:<<your-port>> \
  --security.certificate=<<your-cert-path>>/resource_cert.crt \
  --security.private-key=<<your-cert-path>>/resource_cert.key \
  --aas.aas-registry.url=<<your-aas-registry-url>> \
  --aas.submodel-registry.url=<<your-submodel-registry-url>> \
  --security.enabled=true \
  --jwt.auth.issuer-uri=<<your-keycloak-url>>/realms/<<your-keycloak-realm>> \
  --datasources.docker.docker-host=unix:///var/run/docker.sock
```

### Systemd Service Unit
To register the self-description-service as a systemd service unit, create a file named `resource-self-description-service.service` in the `/etc/systemd/system/` 
directory with the following content. Make sure to replace the placeholders (e.g. `<<your-resource-id>>`) with your actual values.

`resource-self-description-service.service`
```ini
[Unit]
Description=Resource Self Description Service
#Wants=network-online.target
After=network-online.target

[Service]
User=<<your-user-executing-the-service>>

ExecStart=/usr/bin/java -jar <<your-jar-file-path>> \
  --resource.id=<<your-resource-id>> \
  --server.port=<<your-port>> \
  --deployment.url=https://<<your-hostname>>:<<your-port>> \
  --security.certificate=<<your-cert-path>>/resource_cert.crt \
  --security.private-key=<<your-cert-path>>/resource_cert.key \
  --aas.aas-registry.url=<<your-aas-registry-url>> \
  --aas.submodel-registry.url=<<your-submodel-registry-url>> \
  --security.enabled=true \
  --jwt.auth.issuer-uri=<<your-keycloak-url>>/realms/<<your-keycloak-realm>> \
  --datasources.docker.docker-host=unix:///var/run/docker.sock

StandardOutput=append:<<your-install-path>>/resource-self-description-service.log
StandardError=append:<<your-install-path>>/resource-self-description-service.error.log

[Install]
WantedBy=default.target
```

After creating the service unit file, execute the following commands to reload the systemd daemon, enable the service to start on boot, and start the service:

```shell
systemctl daemon-reload
systemctl enable resource-self-description-service
systemctl start resource-self-description-service
```

Finally, you can check the status of the service:

```shell
systemctl status resource-self-description-service
```

The output indicates if the service is active and running. You can also check the log files specified in the service unit file for more detailed information. 
The output should look similar to this:

```shell
● resource-self-description-service.service - Resource Self Description Service
     Loaded: loaded (/etc/systemd/system/resource-self-description-service.service; disabled; vendor preset: enabled)
     Active: active (running) since Fri 2025-09-26 17:44:39 UTC; 2 weeks 0 days ago
   Main PID: 2272028 (java)
      Tasks: 31 (limit: 2324)
     Memory: 416.1M
        CPU: 1h 3min 560ms
     CGroup: /system.slice/resource-self-description-service.service
             └─2272028 /usr/bin/java -jar /opt/slm/resource-self-description-service/resource-self-description-service-0.0.1-SNAPSHOT.jar --resource.id=5937a99e-e656-4875-bea1-7d3bbfa40adc --server.port=48048 --deployment.url=https://10.17.164.90:48048 --security.certificat>

Sep 26 17:44:39 test-vm-small-13748007-2759-4b26-b82d-c4f0d7a19354 systemd[1]: Started Resource Self Description Service.
```