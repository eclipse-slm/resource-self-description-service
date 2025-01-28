package org.eclipse.slm.self_description_service.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty

data class NodeServicesResponse(

    @JsonProperty("Node")
    var Node: org.eclipse.slm.self_description_service.common.consul.model.catalog.Node,

    @JsonProperty("Services")
    var Services: List<NodeService>?
)
