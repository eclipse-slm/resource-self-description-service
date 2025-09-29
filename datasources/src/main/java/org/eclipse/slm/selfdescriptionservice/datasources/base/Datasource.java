package org.eclipse.slm.selfdescriptionservice.datasources.base;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface Datasource {

    List<Submodel> getSubmodels();

    List<SubmodelMetaData> getMetaDataOfSubmodels();

    Optional<Submodel> getSubmodelById(String id) throws IOException;

    List<? extends DataSourceValueDefinition<?>> getValueDefinitions();

    String getValue(String valueKey);

}
