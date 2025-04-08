package org.eclipse.slm.self_description_service.service.rest.aas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class MethodNotImplementedException extends RuntimeException {

	public MethodNotImplementedException() {
		super("Method not implemented or supported for this submodel");
	}
}
