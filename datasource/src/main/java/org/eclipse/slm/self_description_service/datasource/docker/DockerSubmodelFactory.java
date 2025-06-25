package org.eclipse.slm.self_description_service.datasource.docker;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.self_description_service.datasource.AbstractSubmodelFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DockerSubmodelFactory extends AbstractSubmodelFactory {

    public DockerSubmodelFactory(@Value("${resource.id}") String resourceId) {
        super(resourceId);
    }

    @Override
    public Submodel getSubmodel() {
        var dockerSubmodel = new DockerSubmodel(this.resourceId);

        return dockerSubmodel;
    }

}
