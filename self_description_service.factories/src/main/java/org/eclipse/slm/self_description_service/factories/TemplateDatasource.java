package org.eclipse.slm.self_description_service.factories;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.slm.self_description_service.factories.template.ITemplateManager;
import org.eclipse.slm.self_description_service.factories.template.TemplateManager;
import org.eclipse.slm.self_description_service.templating.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class TemplateDatasource implements Datasource, InitializingBean {
    private final static Logger LOG = LoggerFactory.getLogger(TemplateDatasource.class);

    private ITemplateManager templateManager;
    private TemplateRenderer renderer;

    private final HashMap<String, String> idToFileMap = new HashMap<>();

    public TemplateDatasource(ITemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Autowired
    public TemplateDatasource(TemplateRenderer renderer) {
        this.renderer = renderer;

        templateManager = new TemplateManager();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        var templates = this.templateManager.getTemplates();
        for (Resource template : templates) {
            AASXDeserializer aasxDeserializer = null;
            aasxDeserializer = new AASXDeserializer(template.getInputStream());
            var environment = aasxDeserializer.read();

            for (Submodel submodel : environment.getSubmodels()) {
                this.idToFileMap.put(submodel.getId(), template.getFilename());
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
        ArrayList<String> submodels = new ArrayList<>();

        Resource[] templates;
        AASXDeserializer aasxDeserializer;
        try {
            templates = this.templateManager.getTemplates();
            for (Resource template : templates) {

                aasxDeserializer = new AASXDeserializer(template.getInputStream());
                var environment = aasxDeserializer.read();

                for (Submodel submodel : environment.getSubmodels()) {
                    submodels.add(submodel.getId());
                    if (!idToFileMap.containsKey(submodel.getId())) {
                        idToFileMap.put(submodel.getId(), template.getFilename());
                    }
                }
            }
        } catch (IOException | InvalidFormatException | DeserializationException e) {
            LOG.error("Failed to get model ids with error message: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return submodels;
    }

    @Override
    public Optional<Submodel> getModelById(String id) {
        try {
            if (!this.idToFileMap.containsKey(id)) {
                return searchModelInTemplates(id);
            }

            var resource = this.templateManager.getTemplate(this.idToFileMap.get(id));

            if (resource.isEmpty()) {
                return Optional.empty();
            }


            var aasxDeserializer = new AASXDeserializer(resource.get().getInputStream());
            var environment = aasxDeserializer.read();
            var submodel = environment.getSubmodels().stream().filter(model -> model.getId().equals(id)).findFirst();
            if (submodel.isPresent()) {
                renderSubmodel(submodel.get());
                return submodel;
            }
            return submodel;

        } catch (InvalidFormatException | DeserializationException | IOException e) {
            LOG.error("Failed to get model by id with error message: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void renderSubmodel(Submodel m) {
        if (m != null) {
            for (SubmodelElement submodelElement : m.getSubmodelElements()) {
                if (submodelElement instanceof Property property) {
                    var value = this.renderer.render(property.getValue());
                    property.setValue(value);
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

                return environment.getSubmodels().stream().filter(submodel -> submodel.getId().equals(id)).findFirst();

            }
        } catch (IOException | InvalidFormatException | DeserializationException e) {
            LOG.error("Failed to get model by id with error message: {}", e.getMessage());
            return Optional.empty();
        }

        return Optional.empty();
    }


    public void setTemplateManager(ITemplateManager templateManager) {
        this.templateManager = templateManager;
    }
}
