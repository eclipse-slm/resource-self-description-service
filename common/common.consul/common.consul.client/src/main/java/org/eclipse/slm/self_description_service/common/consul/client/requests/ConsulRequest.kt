package org.eclipse.slm.self_description_service.common.consul.client.requests

import org.eclipse.slm.self_description_service.common.consul.client.requests.parameters.UrlParameters

interface ConsulRequest {
    fun asUrlParameters(): List<UrlParameters?>?
}
