package org.eclipse.slm.self_description_service.service.rest.registration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.self_description_service.common.aas.clients.aas.AasRegistryClient;
import org.eclipse.slm.self_description_service.common.aas.clients.aas.AasRepositoryClient;
import org.eclipse.slm.self_description_service.common.aas.clients.aas.SubmodelRegistryClient;
import org.eclipse.slm.self_description_service.common.consul.client.ConsulCredential;
import org.eclipse.slm.self_description_service.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.self_description_service.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.self_description_service.datasource.DatasourceService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AssRegistration implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(AssRegistration.class);

    private final Environment env;
    
    private final ConsulCredential consulCredential;
    private final ConsulServicesApiClient consulClient;
    private final ObjectMapper objectMapper;
    private final DatasourceService datasourceService;
    private final String resourceId;
    private final String submodelRepositoryUrl;
    public AssRegistration(ConsulServicesApiClient consulClient, ObjectMapper objectMapper, DatasourceService datasourceService, @Value("${resource.aas.id}") String resourceId, @Value("${aas.submodel-repository.url}") String submodelRepositoryUrl, Environment env) {
        this.consulClient = consulClient;
        this.datasourceService = datasourceService;
        this.resourceId = resourceId;
        this.submodelRepositoryUrl = submodelRepositoryUrl;
        this.consulCredential = new ConsulCredential();
        this.objectMapper = objectMapper;
        this.env = env;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        arePropertiesSet();
        AASClients clients = getAasClients();

        // Get AAS and Submodel Registry
        isAASAvailable(clients.aasRegistryClient());
        registerSubmodelReferencesToResourceAAS(clients.aasRepositoryClient());

        // Check if Submodels are registered
        // Register/Unregister Submodels
        registerSubmodelsToSubmodelRegistry(clients.aasRepositoryClient(), clients.submodelRegistryClient());
    }

    @NotNull
    private AASClients getAasClients() {

        var useConfigUrls = env.getProperty("resource.use-config-urls", boolean.class);

        String aasRegistryUrl;
        String aasRepositoryUrl;
        String submodelRegistryUrl;

        if (Boolean.TRUE.equals(useConfigUrls)) {
            aasRegistryUrl = env.getProperty("aas.aas-registry.url");
            aasRepositoryUrl = env.getProperty("aas.aas-repository.url");
            submodelRegistryUrl = env.getProperty("aas.submodel-registry.url");
        }else {
            var names = List.of("aas-registry", "aas-repository", "submodel-registry");
            var services = getServices(names);
            aasRegistryUrl = this.getServiceUrl(services, "aas-registry", "http");
            aasRepositoryUrl = this.getServiceUrl(services, "aas-repository", "http");
            submodelRegistryUrl = this.getServiceUrl(services, "submodel-registry", "http");
        }

        LOG.info("AAS Registry URL: {}", aasRegistryUrl);
        LOG.info("AAS Repository URL: {}", aasRepositoryUrl);
        LOG.info("AAS Submodel Registry URL: {}", submodelRegistryUrl);

        var aasRegistryClient = new AasRegistryClient(aasRegistryUrl, aasRepositoryUrl, this.objectMapper);
        var aasRepositoryClient = new AasRepositoryClient(aasRepositoryUrl);
        var submodelRegistryClient = new SubmodelRegistryClient(submodelRegistryUrl, this.submodelRepositoryUrl);
        return new AASClients(aasRegistryClient, aasRepositoryClient, submodelRegistryClient);
    }

    private void registerSubmodelsToSubmodelRegistry(@NotNull AasRepositoryClient aasRepositoryClient, @NotNull SubmodelRegistryClient submodelRegistryClient) throws org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException {
        var registeredSubmodels = submodelRegistryClient.getAllSubmodelDescriptors().stream().collect(Collectors.toMap(SubmodelDescriptor::getId, submodelDescriptor -> submodelDescriptor));
        var submodelIds = new HashSet<>(this.datasourceService.getSubmodelIds());
        for (String submodelId : submodelIds) {
            if (registeredSubmodels.containsKey(submodelId)) {
                submodelRegistryClient.unregisterSubmodel(submodelId);
            }

            var model = this.datasourceService.getSubmodelById(submodelId);
            if (model.isPresent()) {
                var submodel = model.get();
                var modelIdEncoded = new Base64UrlEncodedIdentifier(submodel.getId());
                var url = this.submodelRepositoryUrl + "/" + modelIdEncoded.getEncodedIdentifier();
                submodelRegistryClient.registerSubmodel(url, submodel.getId(), submodel.getId(), submodel.getSemanticId().toString());
            }
        }

        for (SubmodelDescriptor submodel : registeredSubmodels.values()) {
            var endpoints = submodel.getEndpoints();
            for (Endpoint endpoint : endpoints) {
                if (endpoint.getProtocolInformation().getHref().contains(this.submodelRepositoryUrl) &&
                        !submodelIds.contains(submodel.getId())) {
                    aasRepositoryClient.removeSubmodelReferenceFromAas(this.resourceId, submodel.getId());
                    submodelRegistryClient.unregisterSubmodel(submodel.getId());
                }
            }
        }
    }

    private void registerSubmodelReferencesToResourceAAS(@NotNull AasRepositoryClient client) {
        var aasSubmodelIDs = client.getAas(this.resourceId)
                .getSubmodels().stream().map(Reference::getKeys).flatMap(Collection::stream)
                .map(Key::getValue).collect(Collectors.toSet());
        var submodelIds = new HashSet<>(this.datasourceService.getSubmodelIds());
        for (var submodelID : submodelIds) {
            if (aasSubmodelIDs.contains(submodelID)) {
                client.removeSubmodelReferenceFromAas(this.resourceId, submodelID);
            }

            client.addSubmodelReferenceToAas(this.resourceId, submodelID);
        }
    }

    private void arePropertiesSet() {
        if (this.resourceId.isEmpty()) {
            throw new RuntimeException("AAS ID not set");
        }

        if (this.submodelRepositoryUrl.isEmpty()) {
            throw new RuntimeException("Submodel repository URL not set");
        }
    }

    private void isAASAvailable(@NotNull AasRegistryClient aasRegistryClient) throws ApiException {
        var optionalAasDescriptor = aasRegistryClient.getAasDescriptor(this.resourceId);
        if (optionalAasDescriptor.isEmpty()) {
            // TODO: LOG
            throw new RuntimeException("No AAS descriptor found");
        }
    }

    private Map<String, List<CatalogService>> getServices(List<String> names) {
        try {
            return this.consulClient.getServicesByName(consulCredential, names);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String getServiceUrl(@NotNull Map<String, List<CatalogService>> services, String key, String schema) {
        var optionalService = services.get(key).stream().findFirst();
        return optionalService.map(service -> this.createUrl(schema, service))
                .orElseGet(() -> env.getProperty(String.format("aas.%s.url", key)));
    }

    @NotNull
    private String createUrl(String schema, @NotNull CatalogService service) {
        return createUrl(schema, service.getServiceAddress(), service.getServicePort());
    }

    @NotNull
    @Contract(pure = true)
    private String createUrl(String schema, String name, Integer port) {
        return schema + "://" + name + ":" + port;
    }

    private record AASClients(AasRegistryClient aasRegistryClient, AasRepositoryClient aasRepositoryClient,
                              SubmodelRegistryClient submodelRegistryClient) {
    }
}
