package org.eclipse.slm.self_description_service.datasource.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.self_description_service.datasource.AbstractDatasource;
import org.eclipse.slm.self_description_service.datasource.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DockerDatasourceService extends AbstractDatasource implements Datasource {
    private final static Logger LOG = LoggerFactory.getLogger(DockerDatasourceService.class);


    DockerClient dockerClient;

    public DockerDatasourceService(@Value("${resource.id}") String resourceId) {
        super(resourceId, "Docker");
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .withDockerTlsVerify(false)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    @Override
    public List<Submodel> getModels() {
        try {
            return this.getModelsByGenericCode();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return List.of();
        }
    }

    public List<Submodel> getModelsByGenericCode() {

        // TODO: Get ResourceID and use it in the ID to be global unique

        var dockerSubmodel = new DockerSubmodel(this.resourceId);


        var containers = this.dockerClient.listContainersCmd().exec();
        dockerSubmodel.addSubmodelEntry("Containers", containers);

        var images = this.dockerClient.listImagesCmd().exec();
        dockerSubmodel.addSubmodelEntry("Images", images);

        var networks = this.dockerClient.listNetworksCmd().exec();
        dockerSubmodel.addSubmodelEntry("Networks", networks);

        var volumes = this.dockerClient.listVolumesCmd().exec().getVolumes();
        dockerSubmodel.addSubmodelEntry("Volumes", volumes);

        try {
            var services = this.dockerClient.listServicesCmd().exec();
            dockerSubmodel.addSubmodelEntry("Services", services);

            var tasks = this.dockerClient.listTasksCmd().exec();
            dockerSubmodel.addSubmodelEntry("Tasks", tasks);

            var swarmNodes = this.dockerClient.listSwarmNodesCmd().exec();
            dockerSubmodel.addSubmodelEntry("Swarm Nodes", swarmNodes);

            var configs = this.dockerClient.listConfigsCmd().exec();
            dockerSubmodel.addSubmodelEntry("Configs", configs);

            var secrets = this.dockerClient.listSecretsCmd().exec();
            dockerSubmodel.addSubmodelEntry("Secrets", secrets);
        } catch (DockerException exception) {
            LOG.info("Docker runs not in Swarm mode ");
        }

        return List.of(dockerSubmodel);
    }

    @Override
    public List<String> getModelIds() {
        try {
            return this.getModelsByGenericCode().stream().map(Identifiable::getId).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return List.of();
        }
    }

    @Override
    public Optional<Submodel> getModelById(String id) throws IOException {
        var models = this.getModelsByGenericCode();
        for (Submodel model : models) {
            if (model.getId().equals(id)) {
                return Optional.of(model);
            }
        }
        return Optional.empty();
    }

}
