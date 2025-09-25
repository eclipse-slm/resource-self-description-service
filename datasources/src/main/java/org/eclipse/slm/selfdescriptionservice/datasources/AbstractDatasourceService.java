package org.eclipse.slm.selfdescriptionservice.datasources;

import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractDatasourceService implements Datasource {

    protected String resourceId = "";

    protected String datasourceName = "";

    protected AbstractDatasourceService(@Value("resource.id") String resourceId, String datasourceName) {
        this.resourceId = resourceId;
        this.datasourceName = datasourceName;
    }

    public String getDatasourceName() {
        return this.datasourceName;
    }

    protected String createSubmodelId(String id) {
        return this.datasourceName + "-" + id + "-" + this.resourceId;
    }
}
