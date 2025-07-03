package org.eclipse.slm.self_description_service.datasource;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractSubmodelFactory {

    protected final String resourceId;

    public AbstractSubmodelFactory(@Value("${resource.id}") String resourceId) {
        this.resourceId = resourceId;
    }

    public abstract Submodel getSubmodel();

}
