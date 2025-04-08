package org.eclipse.slm.self_description_service.service.rest.aas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubmodelNotFoundException extends RuntimeException {

    public SubmodelNotFoundException(String submodelId) {
        super("Submodel " + submodelId + " not found");
    }
}
