package org.eclipse.slm.self_description_service.common.consul.client.requests.parameters

interface UrlParameters {
    fun toUrlParameters(): List<String?>?
}
