package org.eclipse.slm.self_description_service.common.consul.model.authentication

class LoginRequest {
    var authMethod = "keycloak"
    var bearerToken: String? = null

    constructor() {}

    constructor(bearerToken: String?) {
        this.bearerToken = bearerToken
    }

    constructor(authMethod: String, bearerToken: String?) {
        this.authMethod = authMethod
        this.bearerToken = bearerToken
    }
}
