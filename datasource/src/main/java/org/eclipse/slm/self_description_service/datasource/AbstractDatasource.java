package org.eclipse.slm.self_description_service.datasource;

import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractDatasource {

    protected String resourceId = "";
    protected String resourceName = "";

    public AbstractDatasource(@Value("resource.id") String resourceId, String resourceName) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
    }

    protected String createSubmodelId(String id) {
        return this.resourceName + "-" + id + "-" + this.resourceId;
    }
}
