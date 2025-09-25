package org.eclipse.slm.selfdescriptionservice.datasources.docker;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.selfdescriptionservice.datasources.AbstractSubmodelFactory;
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
