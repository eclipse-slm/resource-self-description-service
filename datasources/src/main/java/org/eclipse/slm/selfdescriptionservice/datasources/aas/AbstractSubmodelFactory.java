package org.eclipse.slm.selfdescriptionservice.datasources.aas;

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
