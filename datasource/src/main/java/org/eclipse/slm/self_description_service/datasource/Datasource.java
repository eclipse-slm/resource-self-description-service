package org.eclipse.slm.self_description_service.datasource;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface Datasource {

    List<Submodel> getSubmodels();

    List<String> getSubmodelIds();

    Optional<Submodel> getSubmodelById(String id) throws IOException;

}
