package org.eclipse.slm.selfdescriptionservice.datasources;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface Datasource {

    List<Submodel> getSubmodels();

    List<String> getSubmodelIds();

    Optional<Submodel> getSubmodelById(String id) throws IOException;

}
