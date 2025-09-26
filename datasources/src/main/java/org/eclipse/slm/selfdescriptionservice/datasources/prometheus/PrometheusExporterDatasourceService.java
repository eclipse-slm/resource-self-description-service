package org.eclipse.slm.selfdescriptionservice.datasources.prometheus;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.selfdescriptionservice.datasources.AbstractDatasourceService;
import org.eclipse.slm.selfdescriptionservice.datasources.template.datasourcevalues.DataSourceValueRegistry;
import org.eclipse.slm.selfdescriptionservice.datasources.aas.SubmodelMetaData;
import org.eclipse.slm.selfdescriptionservice.datasources.docker.DataSourceValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Datasource service for Prometheus Exporter.
 * See {@link AbstractDatasourceService} for base logic.
 */
@Component
public class PrometheusExporterDatasourceService extends AbstractDatasourceService {

    public static final String DATASOURCE_NAME = "PrometheusExporter";

    /**
     * Constructor for PrometheusExporterDatasourceService.
     * @param resourceId The resource ID
     * @param dataSourceValueRegistry The registry for DataSourceValues
     */
    public PrometheusExporterDatasourceService(
            @Value("${resource.id}") String resourceId,
            DataSourceValueRegistry dataSourceValueRegistry) {
        super(resourceId, "Prometheus", dataSourceValueRegistry);
    }


    //region AbstractDatasourceService
    @Override
    public List<Submodel> getSubmodels() {
        return List.of();
    }

    @Override
    public List<SubmodelMetaData> getMetaDataOfSubmodels() {
        return List.of();
    }

    @Override
    public Optional<Submodel> getSubmodelById(String id) throws IOException {
        return Optional.empty();
    }

    @Override
    protected List<? extends DataSourceValue<?>> getDataSourceValues() {
        return List.of();
    }
    //endregion AbstractDatasourceService
}
