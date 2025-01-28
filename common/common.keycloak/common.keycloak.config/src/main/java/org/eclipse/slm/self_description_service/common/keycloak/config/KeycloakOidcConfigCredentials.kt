package org.eclipse.slm.self_description_service.common.keycloak.config

import com.fasterxml.jackson.annotation.JsonProperty

class KeycloakOidcConfigCredentials(

    @JsonProperty("secret")
    val secret: String

) {
}