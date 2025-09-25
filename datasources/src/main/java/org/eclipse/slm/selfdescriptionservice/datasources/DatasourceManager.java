package org.eclipse.slm.selfdescriptionservice.datasources;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class DatasourceManager {

    private final static Logger LOG = LoggerFactory.getLogger(DatasourceManager.class);

    private final Map<String, Datasource> submodelToDatasourceMap = new HashMap<>();
    private final List<AbstractDatasourceService> datasourceServices;

    public DatasourceManager(List<AbstractDatasourceService> datasourceServices) {
        this.datasourceServices = datasourceServices;

        for (var datasource : this.datasourceServices) {
            var modelIds = datasource.getSubmodelIds();
            for (String id : modelIds) {
                this.submodelToDatasourceMap.put(id, datasource);
            }
        }
    }

    public List<Datasource> getDatasourceServices() {
        return new ArrayList<>(this.datasourceServices);
    }

    public Optional<Datasource> getDatasourceForSubmodelId(String submodelId) {
        if (!this.submodelToDatasourceMap.containsKey(submodelId)) {
            for (var datasource : datasourceServices) {
                try {
                    var submodel = datasource.getSubmodelById(submodelId);
                    if (submodel.isPresent()) {
                        this.submodelToDatasourceMap.put(submodelId, datasource);
                        return Optional.of(datasource);
                    }
                } catch (IOException e) {
                    throw new ElementDoesNotExistException();
                }
            }
        } else {
            var datasource = this.submodelToDatasourceMap.get(submodelId);
            return Optional.of(datasource);
        }

        return Optional.empty();
    }


    public Set<String> getSubmodelIds() {
        return this.submodelToDatasourceMap.keySet();
    }

    public Optional<Submodel> getSubmodelById(String submodelId) {
        var optionalDatasource = this.getDatasourceForSubmodelId(submodelId);
        if (optionalDatasource.isEmpty()) {
            return Optional.empty();
        }
        try {
            var datasource = optionalDatasource.get();
            return datasource.getSubmodelById(submodelId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Submodel> getSubmodels() {
        ArrayList<Submodel> submodels = new ArrayList<>();
        for (Datasource datasource : this.datasourceServices) {
            submodels.addAll(datasource.getSubmodels());
        }

        return submodels;
    }
}
