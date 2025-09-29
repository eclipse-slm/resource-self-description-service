package org.eclipse.slm.selfdescriptionservice.datasources;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.slm.selfdescriptionservice.datasources.base.SubmodelMetaData;
import org.eclipse.slm.selfdescriptionservice.datasources.base.AbstractDatasource;
import org.eclipse.slm.selfdescriptionservice.datasources.base.DataSourceValueNotFoundException;
import org.eclipse.slm.selfdescriptionservice.datasources.base.Datasource;
import org.eclipse.slm.selfdescriptionservice.datasources.base.DataSourceValueDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class DatasourceService {

    private final static Logger LOG = LoggerFactory.getLogger(DatasourceService.class);

    private final List<AbstractDatasource> datasources;
    private final Map<String, Datasource> submodelToDatasourceMap = new HashMap<>();
    private final Map<String, DataSourceValueDefinition<?>> datasourceValueKeyToDefinition = new HashMap<>();

    public DatasourceService(List<AbstractDatasource> datasources) {
        this.datasources = datasources;
        for (var datasource : this.datasources) {
            var metaDataOfSubmodels = datasource.getMetaDataOfSubmodels();
            // Get all submodels meta data
            for (var submodelMetaData : metaDataOfSubmodels) {
                this.submodelToDatasourceMap.put(submodelMetaData.getId(), datasource);
            }
            // Get all supported data source values
            for (DataSourceValueDefinition<?> valueDefinition : datasource.getValueDefinitions()) {
                datasourceValueKeyToDefinition.put(valueDefinition.getKey(), valueDefinition);
            }
        }
    }

    public List<Datasource> getDatasources() {
        return new ArrayList<>(this.datasources);
    }

    public Optional<Datasource> getDatasourceForSubmodelId(String submodelId) {
        if (!this.submodelToDatasourceMap.containsKey(submodelId)) {
            for (var datasource : datasources) {
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


    public List<SubmodelMetaData> getMetaDataOfSubmodels() {
        var allMetaDataOfSubmodel = new ArrayList<SubmodelMetaData>();
        for (var datasource : this.datasources) {
            var metaDataOfSubmodels = datasource.getMetaDataOfSubmodels();
            allMetaDataOfSubmodel.addAll(metaDataOfSubmodels);
        }

        return allMetaDataOfSubmodel;
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
        for (Datasource datasource : this.datasources) {
            submodels.addAll(datasource.getSubmodels());
        }

        return submodels;
    }

    /**
     * Returns the value for the given key, if registered.
     * @param key The key of the requested value
     * @return Value as String
     * @throws DataSourceValueNotFoundException if the key is not found
     * @see DataSourceValueNotFoundException
     * @see DataSourceValueDefinition#getValue()
     */
    public String getDatasourceValue(String key) {
        DataSourceValueDefinition<?> value = datasourceValueKeyToDefinition.get(key);
        if (value == null) {
            throw new DataSourceValueNotFoundException("DataSourceValueRegistry", key);
        }
        return String.valueOf(value.getValue());
    }

    public  Map<String, DataSourceValueDefinition<?>> getDatasourceValueDefinitions() {
        return this.datasourceValueKeyToDefinition;
    }
}
