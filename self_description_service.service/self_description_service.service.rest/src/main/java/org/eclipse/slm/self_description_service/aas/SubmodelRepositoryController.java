package org.eclipse.slm.self_description_service.aas;

import org.eclipse.slm.self_description_service.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.self_description_service.http.pagination.Base64UrlEncodedCursor;
import org.eclipse.slm.self_description_service.http.pagination.PagedResult;
import org.springframework.http.ResponseEntity;

public class SubmodelRepositoryController implements SubmodelRepositoryHTTPApi{



    @Override
    public ResponseEntity<PagedResult> getAllSubmodels(Base64UrlEncodedIdentifier semanticId, String idShort, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
        return null;
    }
}
