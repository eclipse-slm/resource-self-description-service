package org.eclipse.slm.selfdescriptionservice.app.aas;

import jakarta.annotation.PostConstruct;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.selfdescriptionservice.datasources.DatasourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AasService {

    private static final Logger LOG = LoggerFactory.getLogger(AasService.class);

    private final DatasourceManager datasourceManager;
    private final String aasId;
    private final String deploymentUrl;

    private final AasRegistryClientFactory aasRegistryClientFactory;
    private final SubmodelRegistryClientFactory submodelRegistryClientFactory;

    public AasService(@Value("${resource.aas.id}") String aasId,
                      @Value("${deployment.url}") String deploymentUrl,
                      DatasourceManager datasourceManager,
                      AasRegistryClientFactory aasRegistryClientFactory,
                      SubmodelRegistryClientFactory submodelRegistryClientFactory) {
        this.datasourceManager = datasourceManager;
        this.aasId = aasId;
        this.deploymentUrl = deploymentUrl;
        this.aasRegistryClientFactory = aasRegistryClientFactory;
        this.submodelRegistryClientFactory = submodelRegistryClientFactory;
    }

    @PostConstruct
    public void init() throws Exception {
        var shellDescriptorOptional = this.aasRegistryClientFactory.getClient().getAasDescriptor(this.aasId);
        if (shellDescriptorOptional.isEmpty()) {
            throw new IllegalStateException("AAS with ID '" + this.aasId + "' not found in AAS registry. Self-description service requires existing AAS.");
        }

        LOG.info("AAS with ID '" + this.aasId + "' found in AAS registry, using endpoint '" + shellDescriptorOptional.get().getEndpoints().getFirst().getProtocolInformation().getHref() + "'");
        var aasRepositoryClient = AasRepositoryClientFactory.FromShellDescriptor(shellDescriptorOptional.get());
        this.registerSubmodels();
        this.addSubmodelReferencesToResourceAAS(aasRepositoryClient);
    }

    private void registerSubmodels()
            throws org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException {
        var metaDataOfSubmodels = new HashSet<>(this.datasourceManager.getMetaDataOfSubmodels());
        for (var submodelMetaData : metaDataOfSubmodels) {
                var submodelIdEncoded = new Base64UrlEncodedIdentifier(submodelMetaData.getId());
                var url = this.deploymentUrl + "/submodels/" + submodelIdEncoded.getEncodedIdentifier();
                this.submodelRegistryClientFactory.getClient().registerSubmodel(
                        url,
                        submodelMetaData.getId(),
                        submodelMetaData.getId(),
                        submodelMetaData.getSemanticId());
                LOG.info("Registered submodel with ID '{}' to submodel registry at URL '{}'", submodelMetaData.getId(), url);
            }
    }

    private void addSubmodelReferencesToResourceAAS(AasRepositoryClient aasRepositoryClient) {
        var aasSubmodelIds = aasRepositoryClient.getAas(this.aasId)
                .get()
                .getSubmodels().stream().map(Reference::getKeys).flatMap(Collection::stream)
                .map(Key::getValue).collect(Collectors.toSet());
        var metaDataOfSubmodels = new HashSet<>(this.datasourceManager.getMetaDataOfSubmodels());
        for (var submodelMetaData : metaDataOfSubmodels) {
            if (!aasSubmodelIds.contains(submodelMetaData)) {
                aasRepositoryClient.addSubmodelReferenceToAas(this.aasId, submodelMetaData.getId());
                LOG.info("Added submodel reference with ID '{}' to AAS with ID '{}'", submodelMetaData.getId(), this.aasId);
            }
        }
    }
}
