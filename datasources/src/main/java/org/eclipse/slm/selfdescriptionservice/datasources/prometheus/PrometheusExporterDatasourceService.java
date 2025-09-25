package org.eclipse.slm.selfdescriptionservice.datasources.prometheus;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.selfdescriptionservice.datasources.AbstractDatasourceService;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PrometheusExporterDatasourceService extends AbstractDatasourceService {

    public static final String DATASOURCE_NAME = "PrometheusExporter";

    public PrometheusExporterDatasourceService(@Value("${resource.id}") String resourceId) {
        super(resourceId, "Prometheus");
    }

    @Override
    public List<Submodel> getSubmodels() {
        return List.of();
    }

    @Override
    public List<String> getSubmodelIds() {
        return List.of();
    }

    @Override
    public Optional<Submodel> getSubmodelById(String id) throws IOException {
        return Optional.empty();
    }


}
