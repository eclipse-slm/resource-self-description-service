package org.eclipse.slm.selfdescriptionservice.app.aas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubmodelNotFoundException extends RuntimeException {

    public SubmodelNotFoundException(String submodelId) {
        super("Submodel " + submodelId + " not found");
    }
}
