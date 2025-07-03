package org.eclipse.slm.self_description_service.common.aas.clients.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;
import org.eclipse.digitaltwin.basyx.aasrepository.client.internal.AssetAdministrationShellRepositoryApi;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingSubmodelReferenceException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class AasRepositoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(AasRepositoryClient.class);

    private String aasRepositoryUrl;

    private final ConnectedAasRepository connectedAasRepository;

    private final DiscoveryClient discoveryClient;

    private final String aasRepositoryDiscoveryInstanceId = "aas-repository";

    @Autowired
    public AasRepositoryClient(@Value("${aas.aas-repository.url}") String aasRepositoryUrl,
                               DiscoveryClient discoveryClient
                               ) {
        this.aasRepositoryUrl = aasRepositoryUrl;
        this.discoveryClient = discoveryClient;

        if (discoveryClient != null) {
            var aasRepositoryServiceInstances = this.discoveryClient.getInstances(aasRepositoryDiscoveryInstanceId);
            if (aasRepositoryServiceInstances.size() > 0) {
                var aasRepositoryServiceInstance = aasRepositoryServiceInstances.get(0);
                var path = "";
                if (aasRepositoryServiceInstance.getMetadata().get("path") != null) {
                    path = aasRepositoryServiceInstance.getMetadata().get("path");
                }
                if (aasRepositoryServiceInstance != null) {
                    this.aasRepositoryUrl = "http://" + aasRepositoryServiceInstance.getHost()
                            + ":" + aasRepositoryServiceInstance.getPort() + path;
                }
            }
            else {
                LOG.warn("No service instance '" + aasRepositoryDiscoveryInstanceId + "' found via discovery client. Using default URL '"
                        + this.aasRepositoryUrl + "' from application.yml or provided via constructor.");
            }
        }

        var apiClient = ClientUtils.getApiClient(aasRepositoryUrl, null);
        var aasShellRepoApi = new AssetAdministrationShellRepositoryApi(apiClient);
        this.connectedAasRepository = new ConnectedAasRepository(this.aasRepositoryUrl, aasShellRepoApi);
    }

    public AasRepositoryClient(String aasRepositoryUrl) {
        this(aasRepositoryUrl, null);
    }

    public static AasRepositoryClient FromShellDescriptor(AssetAdministrationShellDescriptor shellDescriptor) {
        var shellEndpoint = shellDescriptor.getEndpoints().get(0).getProtocolInformation().getHref();

        if (shellEndpoint.contains("/shells/")) {
            var regExPattern = Pattern.compile("(.*)/shells");
            var matcher = regExPattern.matcher(shellEndpoint);
            var matchesFound = matcher.find();
            if (matchesFound) {
                var aasRepositoryBaseUrl = matcher.group(1);
                var aasRepositoryClient = new AasRepositoryClient(aasRepositoryBaseUrl);

                return aasRepositoryClient;
            }
        }

        LOG.error("Could not create AasRepositoryClient from shell descriptor with endpoint '" + shellEndpoint + "'. No valid endpoint found.");
        throw new IllegalArgumentException("Could not create AasRepositoryClient from shell descriptor with endpoint '" + shellEndpoint + "'. No valid endpoint found.");
    }

    public void createOrUpdateAas(AssetAdministrationShell aas) {
        try {
            this.connectedAasRepository.createAas(aas);
        } catch (CollidingIdentifierException e) {
            this.connectedAasRepository.updateAas(aas.getId(), aas);
        }
        catch (RuntimeException e) {
            LOG.error(e.getMessage());
        }
    }

    public Optional<AssetAdministrationShell> getAas(String aasId) {
        try {
            this.connectedAasRepository.getAas(aasId);
        } catch (ElementDoesNotExistException e) {
            LOG.error("AAS with id '{}' does not exist", aasId);
            return Optional.empty();
        } catch (RuntimeException e) {
            LOG.error("Error while retrieving AAS with id {}: {}", aasId, e.getMessage());
            return Optional.empty();
        }
        var aas = this.connectedAasRepository.getAas(aasId);

        return Optional.of(aas);
    }

    public void addSubmodelReferenceToAas(String aasId, Submodel submodel) {
        this.addSubmodelReferenceToAas(aasId, submodel.getId());
    }

    public void addSubmodelReferenceToAas(String aasId, String smId) {
        try {
            var submodelReference = new DefaultReference.Builder()
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.SUBMODEL)
                            .value(smId).build())
                    .build();
            this.connectedAasRepository.addSubmodelReference(aasId, submodelReference);
        } catch (CollidingSubmodelReferenceException e) {
            LOG.debug("Submodel reference already exists");
        } catch (ElementDoesNotExistException e) {
            LOG.error("AAS with id {} does not exist", aasId);
        }
    }

    public void removeSubmodelReferenceFromAas(String aasId, String smId) {
        this.connectedAasRepository.removeSubmodelReference(aasId, smId);
    }

    public void deleteAAS(String aasId) {
        this.connectedAasRepository.deleteAas(aasId);
    }
}
