package org.eclipse.slm.selfdescriptionservice.datasources.base;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

public class SubmodelMetaData {

    private final String id;
    private final String idShort;
    private final Reference semanticId;

    public SubmodelMetaData(String id, String idShort, Reference semanticId) {
        this.id = id;
        this.idShort = idShort;
        this.semanticId = semanticId;
    }

    public String getId() {
        return id;
    }

    public String getIdShort() {
        return idShort;
    }

    public Reference getSemanticId() {
        return semanticId;
    }
}
