package org.eclipse.slm.selfdescriptionservice.datasources.template;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.slm.selfdescriptionservice.datasources.AbstractDatasourceService;
import org.eclipse.slm.selfdescriptionservice.datasources.template.datasourcevalues.DataSourceValueRegistry;
import org.eclipse.slm.selfdescriptionservice.datasources.aas.SubmodelMetaData;
import org.eclipse.slm.selfdescriptionservice.datasources.docker.DataSourceValue;
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
public class TemplateDatasourceService extends AbstractDatasourceService implements InitializingBean {

    private final static Logger LOG = LoggerFactory.getLogger(TemplateDatasourceService.class);

    public static final String DATASOURCE_NAME = "Template";

    private final static String ID_PREFIX = "Template";

    private final ITemplateManager templateManager;
    private final TemplateRenderer renderer;

    private final HashMap<String, String> idToFileMap = new HashMap<>();

    /**
     * Constructor for TemplateDatasourceService.
     * @param templateManager The template manager
     * @param renderer The template renderer
     * @param resourceId The resource ID
     * @param dataSourceValueRegistry The registry for DataSourceValues
     */
    public TemplateDatasourceService(ITemplateManager templateManager, TemplateRenderer renderer, @Value("${resource.id}") String resourceId, DataSourceValueRegistry dataSourceValueRegistry) {
        super(resourceId, ID_PREFIX, dataSourceValueRegistry);
        this.renderer = renderer;
        this.templateManager = templateManager;
    }

    @Override
    public void afterPropertiesSet() {
        try{
            var templates = this.templateManager.getTemplates();
            for (Resource template : templates) {
                var aasxDeserializer = new AASXDeserializer(template.getInputStream());
                var environment = aasxDeserializer.read();

                for (Submodel submodel : environment.getSubmodels()) {
                    var id = createSubmodelId(submodel.getIdShort());
                    this.idToFileMap.put(id, template.getFilename());
                }

            }
        }catch (Exception e){
            LOG.error(e.getMessage());
        }
    }


    public List<Submodel> getSubmodels() {

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

    private void renderSubmodel(Submodel submodel) {
        if (submodel != null) {
            for (SubmodelElement element : submodel.getSubmodelElements()) {
                renderSubmodelElement(element);
            }
        }
    }

    private void renderSubmodelElement(SubmodelElement element) {
        if (element != null) {
            switch (element) {
                case Property property -> {
                    var value = property.getValue();
                    if (value != null) {
                        var renderedValue = this.renderer.render(value);
                        property.setValue(renderedValue);
                    }
                }
                case SubmodelElementList submodelElementList -> {
                    for (SubmodelElement child : submodelElementList.getValue()) {
                        renderSubmodelElement(child);
                    }
                }
                case SubmodelElementCollection submodelElementCollection -> {
                    for (SubmodelElement child : submodelElementCollection.getValue()) {
                        renderSubmodelElement(child);
                    }
                }
                default -> {
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

    //region AbstractDatasourceService
    @Override
    public List<SubmodelMetaData> getMetaDataOfSubmodels() {
        var metaDataOfSubmodels = new ArrayList<SubmodelMetaData>();

        Resource[] templates;
        AASXDeserializer aasxDeserializer;
        try {
            templates = this.templateManager.getTemplates();
            for (Resource template : templates) {

                aasxDeserializer = new AASXDeserializer(template.getInputStream());
                var environment = aasxDeserializer.read();

                for (var submodel : environment.getSubmodels()) {
                    var submodelId = createSubmodelId(submodel.getIdShort());
                    var submodelMetaData = new SubmodelMetaData(
                            submodelId,
                            submodel.getIdShort(),
                            submodel.getSemanticId()
                    );

                    metaDataOfSubmodels.add(submodelMetaData);

                    if (!idToFileMap.containsKey(submodelMetaData.getId())) {
                        idToFileMap.put(submodelMetaData.getId(), template.getFilename());
                    }
                }
            }
        } catch (IOException | InvalidFormatException | DeserializationException e) {
            LOG.error("Failed to get model ids with error message: {}", e.getMessage());
        }

        return metaDataOfSubmodels;
    }

    @Override
    public Optional<Submodel> getSubmodelById(String id) {
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
                var aasx = aasxDeserializer.read();

                optionalSubmodel = aasx.getSubmodels().stream().filter(model -> {
                    var submodelId = createSubmodelId(model.getIdShort());
                    return submodelId.equals(id);
                }).findFirst();
            }

            if (optionalSubmodel.isPresent()) {

                var submodel = optionalSubmodel.get();
                submodel.setId(createSubmodelId(submodel.getIdShort()));
                renderSubmodel(submodel);
                submodel.setKind(ModellingKind.INSTANCE);
                return Optional.of(submodel);
            }

            return optionalSubmodel;

        } catch (InvalidFormatException | DeserializationException | IOException e) {
            LOG.error("Failed to get model by id with error message: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    protected List<? extends DataSourceValue<?>> getDataSourceValues() {
        return List.of();
    }
    //endregion AbstractDatasourceService
}
