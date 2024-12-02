package org.eclipse.slm.self_description_service.factories;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DockerDatasource implements Datasource {
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
