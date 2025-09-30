package org.eclipse.slm.selfdescriptionservice.datasources;

import org.eclipse.slm.selfdescriptionservice.datasources.base.AbstractDatasource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatasourceRegistry {

    private final List<AbstractDatasource> datasources = new ArrayList<>();

    public List<AbstractDatasource> getDatasources() {
        return new ArrayList<>(datasources);
    }

    public void registerDatasource(AbstractDatasource datasource) {
        this.datasources.add(datasource);
    }
}
