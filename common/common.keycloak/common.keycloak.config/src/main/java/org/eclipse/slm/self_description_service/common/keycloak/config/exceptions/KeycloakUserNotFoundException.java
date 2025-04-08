package org.eclipse.slm.self_description_service.common.keycloak.config.exceptions;

import java.util.UUID;

public class KeycloakUserNotFoundException extends Exception {

    public KeycloakUserNotFoundException(UUID keycloakUserId) {
        super("Keycloak user with id '" + keycloakUserId.toString() + "' not found");
    }

}
