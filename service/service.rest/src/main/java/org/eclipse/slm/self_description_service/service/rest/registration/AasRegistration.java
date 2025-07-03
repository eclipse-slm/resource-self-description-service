package org.eclipse.slm.self_description_service.service.rest.registration;


import jakarta.annotation.PostConstruct;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.self_description_service.common.aas.clients.aas.AasRegistryClient;
import org.eclipse.slm.self_description_service.common.aas.clients.aas.AasRepositoryClient;
import org.eclipse.slm.self_description_service.common.aas.clients.aas.SubmodelRegistryClient;
import org.eclipse.slm.self_description_service.datasource.DatasourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AasRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(AasRegistration.class);

    private final DatasourceManager datasourceManager;
    private final String resourceId;
    private final String deploymentUrl;

    private final AasRegistryClient aasRegistryClient;
    private final SubmodelRegistryClient submodelRegistryClient;

    public AasRegistration(@Value("${resource.aas.id}") String resourceId,
                           @Value("${deployment.url}") String deploymentUrl,
                           DatasourceManager datasourceManager,
                           AasRegistryClient aasRegistryClient,
                           SubmodelRegistryClient submodelRegistryClient
                           ) {
        this.datasourceManager = datasourceManager;
        this.resourceId = resourceId;
        this.deploymentUrl = deploymentUrl;
        this.aasRegistryClient = aasRegistryClient;
        this.submodelRegistryClient = submodelRegistryClient;
    }

    @PostConstruct
    public void init() throws Exception {
        var shellDescriptor = this.getShellDescriptor();
        var aasRepositoryClient = AasRepositoryClient.FromShellDescriptor(shellDescriptor);

        this.registerSubmodelsToSubmodelRegistry();
        this.addSubmodelReferencesToResourceAAS(aasRepositoryClient);
    }

    private void registerSubmodelsToSubmodelRegistry()
            throws org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException {
        var submodelIds = new HashSet<>(this.datasourceManager.getSubmodelIds());
        for (String submodelId : submodelIds) {
            var model = this.datasourceManager.getSubmodelById(submodelId);
            if (model.isPresent()) {
                var submodel = model.get();
                var submodelIdEncoded = new Base64UrlEncodedIdentifier(submodel.getId());
                var url = this.deploymentUrl + "/submodels/" + submodelIdEncoded.getEncodedIdentifier();
                this.submodelRegistryClient.registerSubmodel(url, submodel.getId(), submodel.getId(), submodel.getSemanticId().toString());
                LOG.info("Registered submodel with ID '{}' to submodel registry at URL '{}'", submodel.getId(), url);
            }
        }
    }

    private void addSubmodelReferencesToResourceAAS(AasRepositoryClient aasRepositoryClient) {
        var aasSubmodelIDs = aasRepositoryClient.getAas(this.resourceId)
                .get()
                .getSubmodels().stream().map(Reference::getKeys).flatMap(Collection::stream)
                .map(Key::getValue).collect(Collectors.toSet());
        var submodelIds = new HashSet<>(this.datasourceManager.getSubmodelIds());
        for (var submodelID : submodelIds) {
            if (!aasSubmodelIDs.contains(submodelID)) {
                aasRepositoryClient.addSubmodelReferenceToAas(this.resourceId, submodelID);
                LOG.info("Added submodel reference with ID '{}' to AAS with ID '{}'", submodelID, this.resourceId);
            }
        }
    }

    private AssetAdministrationShellDescriptor getShellDescriptor() throws ApiException {
        var optionalShellDescriptor = this.aasRegistryClient.getAasDescriptor(this.resourceId);
        if (optionalShellDescriptor.isEmpty()) {
            LOG.error("No AAS descriptor found for resource ID: {}", this.resourceId);
            throw new RuntimeException("No AAS descriptor found for resource ID '" + this.resourceId + "'");
        }
        else {
            LOG.info("Found AAS descriptor for resource ID {}", this.resourceId);
            return optionalShellDescriptor.get();
        }
    }
}
