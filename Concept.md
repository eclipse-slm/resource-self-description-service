


* Generierung eines AASX Submodel Templates. 
* Service generiert ID anhand  short-id und ResourceID.
* In dem Templated sind mehrere Submodels definiert. 
* Submodels beinhalten platzhalter die bei der Anfrage ausgetauscht werden. 
* Service bietet verschiedenen Datenquellen an. 


Erster Schitt 
   -> XAASX File parsen
   -> RestAPI mit GET zur verfügung stellen 
   -> Jinjava als parser

Startup:
Check for Consul -> Spring implementierung für Consul um Service zu finden

-> Webcam -> schauen ob es einen Virtuelle Kammera gibt.

10.12.2024

- ResourceID over environment variable (1)
    - Configuration for the datasource (...)

- Rest API Exception Handling  (2)
    - Datasource throw exception
    - Server handle and throw an 500 Internal Error with the exception content

- Integration Test (3)
    - HTTP Request for Template and Docker

- Registration at the AAS Submodel Registry and AAS Model Registry (4)


- Prometheus Templating: (...)
    - ID/Type for an exporter -> lookup in config file for exporter -> collect data -> return data 

20.01.2025

- Docker datasource anpassen
- Submodel Element Endpoint
- Auth über Env Variable ein/aus
- Auth Aus, Keycloak, ... -> Einstellbar machen

- Registrierung testen
- Datasource tests
- Registration test

- Github workflow
- Repo

- Playbook für die installation


