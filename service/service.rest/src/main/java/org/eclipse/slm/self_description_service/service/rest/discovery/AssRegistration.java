package org.eclipse.slm.self_description_service.service.rest.discovery;


import org.eclipse.slm.self_description_service.common.consul.client.apis.ConsulAclApiClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class AssRegistration implements InitializingBean {

    private ConsulAclApiClient client;

    public AssRegistration(ConsulAclApiClient client) {
        this.client = client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // Get Consul Client

        // Get AAS and Submodel Registry

        // Get AAS Repository

        // Check if Resource AAS available

        // Check if Submodels are registered
        // Register/Unregister Submodels

        // Add/Remove Submodel references in AAS Repository


    }
}
