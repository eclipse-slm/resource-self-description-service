package org.eclipse.slm.self_description_service.service.rest.registration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.slm.common.aas.clients.AasRegistryClient;
import org.eclipse.slm.common.aas.clients.AasRepositoryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRegistryClient;
import org.eclipse.slm.self_description_service.common.consul.client.ConsulCredential;
import org.eclipse.slm.self_description_service.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.self_description_service.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.self_description_service.datasource.DatasourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssRegistration implements InitializingBean {

    private final ConsulCredential consulCredential;
    private final ConsulServicesApiClient client;
    private final ObjectMapper objectMapper;
    private final DatasourceService datasourceService;

    @Value("resource.aas-id")
    private final String aasId = "";
    @Value("aas.submodel-repository.url")
    private final String submodelRepositoryUrl = "";

    public AssRegistration(ConsulServicesApiClient client, ObjectMapper objectMapper, DatasourceService datasourceService) {
        this.client = client;
        this.datasourceService = datasourceService;
        this.consulCredential = new ConsulCredential();
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // Get AAS and Submodel Registry
        var names = List.of("aas-registry", "aas-repository", "submodel-registry");
        var services = this.client.getServicesByName(consulCredential, names);


        // Get AAS Repository
        if (services.containsKey("aas-registry") && services.containsKey("aas-repository") && services.containsKey("submodel-registry")) {
            var optionalAasRegistry = services.get("aas-registry").stream().findFirst();
            var optionalAasRepository = services.get("aas-repository").stream().findFirst();
            var optionalSubmodelRegistry = services.get("submodel-registry").stream().findFirst();

            // Check if Resource AAS available
            if (optionalAasRegistry.isEmpty() || optionalAasRepository.isEmpty() || optionalSubmodelRegistry.isEmpty()) {
                throw new RuntimeException("No AAS registry or AAS repository found");
            }

            var aasRegistry = optionalAasRegistry.get();
            var aasRepository = optionalAasRepository.get();
            var submodelRegistry = optionalSubmodelRegistry.get();

            var aasRegistryUrl = this.createUrl("http", aasRegistry);
            var aasRepositoryUrl = this.createUrl("http", aasRepository);
            var submodelRegistryUrl = this.createUrl("http", submodelRegistry);

            var aasRegistryClient = new AasRegistryClient(aasRegistryUrl, aasRepositoryUrl, this.objectMapper);
            var aasRepositoryClient = new AasRepositoryClient(aasRepositoryUrl);
            var submodelRegistryClient = new SubmodelRegistryClient(submodelRegistryUrl, "");

            var optionalAasDescriptor = aasRegistryClient.getAasDescriptor(this.aasId);
            if (optionalAasDescriptor.isEmpty()) {
                throw new RuntimeException("No AAS descriptor found");
            }


//            var aasDescriptor = optionalAasDescriptor.get();
//            var submodelIds = this.datasourceService.getSubmodelIds();
//            for (SubmodelDescriptor submodelDescriptor : aasDescriptor.getSubmodelDescriptors()) {
//                submodelIds.remove(submodelDescriptor.getId());
//            }
//
//            for (String submodelId : submodelIds) {
//                var model = this.datasourceService.getSubmodelById(submodelId);
//                if (model.isEmpty()) {
//                    continue;
//                }
//
//                var builder = new SubmodelDescriptorFactory(null, List.of(""),null);
//                aasRegistryClient.addSubmodelDescriptorToAas(submodelId, builder.create(model.get()));
//            }


            try {
                var aas = aasRepositoryClient.getAas(this.aasId);
                var submodelIds = this.datasourceService.getSubmodelIds();
                for (Reference submodel : aas.getSubmodels()) {
                    for (Key key : submodel.getKeys()) {
                        var value = key.getValue();
                        submodelIds.remove(value);
                    }
                }

                for (String submodelId : submodelIds) {
                    aasRepositoryClient.addSubmodelReferenceToAas(this.aasId, submodelId);
                }


            } catch (ElementDoesNotExistException e) {
                // Log

            }

            // Check if Submodels are registered
            // Register/Unregister Submodels
            var registeredSubmodels = submodelRegistryClient.getAllSubmodelDescriptors();
            var submodelIds = this.datasourceService.getSubmodelIds();
            for (SubmodelDescriptor registeredSubmodel : registeredSubmodels) {
                if (!submodelIds.contains(registeredSubmodel.getId())) {
                    var model = this.datasourceService.getSubmodelById(registeredSubmodel.getId());
                    if (model.isPresent()) {
                        submodelRegistryClient.registerSubmodel(this.submodelRepositoryUrl, model.get());
                    }
                }
            }
        }
    }


    private String createUrl(String schema, CatalogService service) {
        return createUrl(schema, service.getAddress(), service.getServicePort());
    }

    private String createUrl(String schema, String name, Integer port) {
        return schema + "://" + name + ":" + port + "/";
    }
}
