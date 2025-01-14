package org.eclipse.slm.self_description_service.common.consul.model.exceptions;

public class ConsulLoginFailedException extends Exception {

    public ConsulLoginFailedException(String message) {
        super(message);
    }

}
