package org.eclipse.slm.self_description_service.aas.factories;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.slm.self_description_service.ITemplateManager;
import org.eclipse.slm.self_description_service.TemplateManager;
import org.eclipse.slm.self_description_service.aas.Datasource;
import org.eclipse.slm.self_description_service.templating.TemplateRenderer;
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

    private ITemplateManager templateManager;
    private TemplateRenderer renderer;

    private HashMap<String, String> idToFileMap = new HashMap<>();

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
        ArrayList<Submodel> submodels = new ArrayList<Submodel>();

        Resource[] templates = null;
        AASXDeserializer aasxDeserializer = null;
        Environment environment = null;
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
            throw new RuntimeException(e);
        }

        return submodels;
    }

    @Override
    public List<String> getModelIds() {
        ArrayList<String> submodels = new ArrayList<String>();

        Resource[] templates = null;
        AASXDeserializer aasxDeserializer = null;
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
            throw new RuntimeException(e);
        }

        return submodels;
    }

    @Override
    public Optional<Submodel> getModelById(String id) throws IOException {

        if (!this.idToFileMap.containsKey(id)) {
            return searchModelInTemplates(id);
        }

        var resource = this.templateManager.getTemplate(this.idToFileMap.get(id));

        if (resource.isEmpty()) {
            return Optional.empty();
        }

        try {
            var aasxDeserializer = new AASXDeserializer(resource.get().getInputStream());
            var environment = aasxDeserializer.read();
            var submodel = environment.getSubmodels().stream().filter(model -> model.getId().equals(id)).findFirst();
            if (submodel.isPresent()) {
                renderSubmodel(submodel.get());
                return submodel;
            }
            return submodel;

        } catch (InvalidFormatException | DeserializationException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderSubmodel(Submodel m) {
        for (SubmodelElement submodelElement : m.getSubmodelElements()) {
            if (submodelElement instanceof Property property) {
                var value = this.renderer.render(property.getValue());
                property.setValue(value);
            }
        }
    }

    private Optional<Submodel> searchModelInTemplates(String id) {

        Resource[] templates = null;
        AASXDeserializer aasxDeserializer = null;
        try {
            templates = this.templateManager.getTemplates();
            for (Resource template : templates) {

                aasxDeserializer = new AASXDeserializer(template.getInputStream());
                var environment = aasxDeserializer.read();

                return environment.getSubmodels().stream().filter(submodel -> submodel.getId().equals(id)).findFirst();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidFormatException ex) {
            throw new RuntimeException(ex);
        } catch (DeserializationException ex) {
            throw new RuntimeException(ex);
        }

        return Optional.empty();
    }


    public void setTemplateManager(ITemplateManager templateManager) {
        this.templateManager = templateManager;
    }
}
