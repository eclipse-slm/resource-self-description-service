package org.eclipse.slm.self_description_service.datasource.prometheus;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.self_description_service.datasource.AbstractDatasource;
import org.eclipse.slm.self_description_service.datasource.Datasource;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PrometheusExporter extends AbstractDatasource implements Datasource {


    public PrometheusExporter(@Value("${resource.id}") String resourceId) {
        super(resourceId, "Prometheus");
    }

    @Override
    public List<Submodel> getModels() {
        return List.of();
    }

    @Override
    public List<String> getModelIds() {
        return List.of();
    }

    @Override
    public Optional<Submodel> getModelById(String id) throws IOException {
        return Optional.empty();
    }


}
