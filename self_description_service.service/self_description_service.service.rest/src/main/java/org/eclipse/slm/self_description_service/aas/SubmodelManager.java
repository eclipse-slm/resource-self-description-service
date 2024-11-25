package org.eclipse.slm.self_description_service.aas;


import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.slm.self_description_service.ITemplateManager;
import org.eclipse.slm.self_description_service.TemplateManager;
import org.eclipse.slm.self_description_service.templating.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class SubmodelManager {

    private ITemplateManager templateManager;

    private TemplateRenderer renderer;

    public SubmodelManager(ITemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Autowired
    public SubmodelManager(TemplateRenderer renderer) {
        this.renderer = renderer;

        templateManager = new TemplateManager();
    }


    public List<Submodel> getSubmodels() throws IOException {
        ArrayList<Submodel> submodels = new ArrayList<Submodel>();

        var jinjavaConfig = JinjavaConfig.newBuilder()
                .withTimeZone(TimeZone.getTimeZone("UTC").toZoneId())
                .build();

        var jinjava = new Jinjava(jinjavaConfig);
        Map<String, Object> context = Maps.newHashMap();

        var templates = this.templateManager.getTemplates();
        for (Resource template : templates){
            AASXDeserializer aasxDeserializer = null;
            try {
                aasxDeserializer = new AASXDeserializer(template.getInputStream());
                var environment = aasxDeserializer.read();


                for (Submodel submodel : environment.getSubmodels()) {
                    for (SubmodelElement submodelElement : submodel.getSubmodelElements()) {
                        if (submodelElement instanceof Property property) {
                            var value = this.renderer.render(property.getValue());
                            property.setValue(value);
                        }
                    }

                }

                submodels.addAll(environment.getSubmodels());

            } catch (InvalidFormatException e) {
                throw new RuntimeException(e);
            } catch (DeserializationException e) {
                throw new RuntimeException(e);
            }

        }



        return submodels;
    }

    public void setTemplateManager(ITemplateManager templateManager) {
        this.templateManager = templateManager;
    }
}
