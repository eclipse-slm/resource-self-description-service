package org.eclipse.slm.self_description_service.common.consul.client

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class ConsulCredential(
    val consulCredentialType: ConsulCredentialType
) {
    var consulToken: String? = null

    var jwtAuthenticationToken: JwtAuthenticationToken? = null

    constructor() : this(ConsulCredentialType.APPLICATION_PROPERTIES) {
    }

    constructor(consulToken: String) : this(ConsulCredentialType.CONSUL_TOKEN) {
        this.consulToken = consulToken
    }

    constructor(jwtAuthenticationToken: JwtAuthenticationToken) : this(ConsulCredentialType.JWT) {
        this.jwtAuthenticationToken = jwtAuthenticationToken
    }
}
