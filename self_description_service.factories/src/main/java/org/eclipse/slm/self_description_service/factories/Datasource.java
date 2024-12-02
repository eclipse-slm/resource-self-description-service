package org.eclipse.slm.self_description_service.factories;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface Datasource {

    List<Submodel> getModels();

    List<String> getModelIds();

    Optional<Submodel> getModelById(String id) throws IOException;

}
