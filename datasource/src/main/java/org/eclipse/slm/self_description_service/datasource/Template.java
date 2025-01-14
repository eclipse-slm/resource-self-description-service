package org.eclipse.slm.self_description_service.datasource;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.slm.self_description_service.datasource.template.ITemplateManager;
import org.eclipse.slm.self_description_service.templating.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class Template implements Datasource, InitializingBean {
    private final static Logger LOG = LoggerFactory.getLogger(Template.class);

    private final String resourceId;

    private ITemplateManager templateManager;
    private TemplateRenderer renderer;

    private final HashMap<String, String> idToFileMap = new HashMap<>();

    public Template(ITemplateManager templateManager, TemplateRenderer renderer, @Value("${resource.id}") String resourceId) {
        this.renderer = renderer;
        this.templateManager = templateManager;
        this.resourceId = resourceId;
    }

//    public Template(ITemplateManager templateManager, @Value("${resource.id}") String resourceId) {
//        this.resourceId = resourceId;
//        this.templateManager = templateManager;
//    }
//
//    @Autowired
//    public Template(TemplateRenderer renderer, @Value("${resource.id}") String resourceId) {
//        this.renderer = renderer;
//        this.resourceId = resourceId;
//
//        templateManager = new TemplateManager();
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        var templates = this.templateManager.getTemplates();
        for (Resource template : templates) {
            AASXDeserializer aasxDeserializer = null;
            aasxDeserializer = new AASXDeserializer(template.getInputStream());
            var environment = aasxDeserializer.read();

            for (Submodel submodel : environment.getSubmodels()) {
                var id = createSubmodelId(submodel.getIdShort());
                this.idToFileMap.put(id, template.getFilename());
            }

        }
    }


    public List<Submodel> getModels() {

        ArrayList<Submodel> submodels = new ArrayList<>();

        Resource[] templates;
        AASXDeserializer aasxDeserializer;
        Environment environment;
        try {
            templates = this.templateManager.getTemplates();
            for (Resource template : templates) {

                aasxDeserializer = new AASXDeserializer(template.getInputStream());
                environment = aasxDeserializer.read();

                for (Submodel submodel : environment.getSubmodels()) {
                    var id = createSubmodelId(submodel.getIdShort());
                    submodel.setId(id);
                    renderSubmodel(submodel);
                }
                submodels.addAll(environment.getSubmodels());
            }
        } catch (IOException | InvalidFormatException | DeserializationException e) {
            LOG.error("Failed to load templates and render with error message: {}", e.getMessage());
            return List.of();
        }

        return submodels;
    }

    @Override
    public List<String> getModelIds() {
        ArrayList<String> submodelIDs = new ArrayList<>();

        Resource[] templates;
        AASXDeserializer aasxDeserializer;
        try {
            templates = this.templateManager.getTemplates();
            for (Resource template : templates) {

                aasxDeserializer = new AASXDeserializer(template.getInputStream());
                var environment = aasxDeserializer.read();

                for (Submodel submodel : environment.getSubmodels()) {
                    var id = createSubmodelId(submodel.getIdShort());
                    submodelIDs.add(id);

                    if (!idToFileMap.containsKey(id)) {
                        idToFileMap.put(id, template.getFilename());
                    }
                }
            }
        } catch (IOException | InvalidFormatException | DeserializationException e) {
            LOG.error("Failed to get model ids with error message: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return submodelIDs;
    }

    @Override
    public Optional<Submodel> getModelById(String id) {
        try {
            Optional<Submodel> optionalSubmodel;
            if (!this.idToFileMap.containsKey(id)) {
                optionalSubmodel = searchModelInTemplates(id);

            } else {
                var resource = this.templateManager.getTemplate(this.idToFileMap.get(id));

                if (resource.isEmpty()) {
                    return Optional.empty();
                }

                var aasxDeserializer = new AASXDeserializer(resource.get().getInputStream());
                var environment = aasxDeserializer.read();

                optionalSubmodel = environment.getSubmodels().stream().filter(model -> {
                    var submodelId = createSubmodelId(model.getIdShort());
                    return submodelId.equals(id);
                }).findFirst();
            }


            if (optionalSubmodel.isPresent()) {

                var submodel = optionalSubmodel.get();
                var submodelId = createSubmodelId(submodel.getIdShort());
                submodel.setId(submodelId);

                renderSubmodel(submodel);
                return Optional.of(submodel);
            }

            return optionalSubmodel;

        } catch (InvalidFormatException | DeserializationException | IOException e) {
            LOG.error("Failed to get model by id with error message: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void renderSubmodel(Submodel submodel) {
        if (submodel != null) {
            for (SubmodelElement element : submodel.getSubmodelElements()) {
                renderSubmodelElement(element);
            }
        }
    }

    private void renderSubmodelElement(SubmodelElement element) {
        if (element != null) {
            if (element instanceof Property property) {
                var value = this.renderer.render(property.getValue());
                property.setValue(value);
            } else if (element instanceof SubmodelElementList submodelElementList) {
                for (SubmodelElement child : submodelElementList.getValue()) {
                    renderSubmodelElement(child);
                }
            } else if (element instanceof SubmodelElementCollection submodelElementCollection) {
                for (SubmodelElement child : submodelElementCollection.getValue()) {
                    renderSubmodelElement(child);
                }
            }
        }
    }

    private Optional<Submodel> searchModelInTemplates(String id) {

        Resource[] templates;
        AASXDeserializer aasxDeserializer;
        try {
            templates = this.templateManager.getTemplates();
            for (Resource template : templates) {

                aasxDeserializer = new AASXDeserializer(template.getInputStream());
                var environment = aasxDeserializer.read();

                return environment.getSubmodels().stream().filter(submodel -> {
                    var submodelId = createSubmodelId(submodel.getIdShort());
                    return submodelId.equals(id);
                }).findFirst();

            }
        } catch (IOException | InvalidFormatException | DeserializationException e) {
            LOG.error("Failed to get model by id with error message: {}", e.getMessage());
            return Optional.empty();
        }

        return Optional.empty();
    }

    private String createSubmodelId(String id) {
        return this.resourceId + "_" + id;
    }
}
