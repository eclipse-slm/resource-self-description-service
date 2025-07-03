package org.eclipse.slm.self_description_service.datasource.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.self_description_service.datasource.AbstractDatasourceService;
import org.eclipse.slm.self_description_service.datasource.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "datasources.docker.enabled", havingValue = "true", matchIfMissing = false)
public class DockerDatasourceService extends AbstractDatasourceService {

    private final static Logger LOG = LoggerFactory.getLogger(DockerDatasourceService.class);

    public static final String DATASOURCE_NAME = "Docker";

    private final DockerSubmodelFactory dockerSubmodelFactory;

    private DockerClient dockerClient;

    public DockerDatasourceService(@Value("${resource.id}") String resourceId,
                                   @Value("${datasources.docker.docker-host}") String dockerHost,
                                   DockerSubmodelFactory dockerSubmodelFactory) {
        super(resourceId, "Docker");
        this.dockerSubmodelFactory = dockerSubmodelFactory;

        LOG.info("Using DOCKER_HOST '{}'", dockerHost);
        var dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(dockerClientConfig, httpClient);
    }

    @Override
    public List<Submodel> getSubmodels() {
        try {
            return this.getModelsByGenericCode();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return List.of();
        }
    }

    public List<Submodel> getModelsByGenericCode() {

        var dockerSubmodel = new DockerSubmodel(this.resourceId);

        var containers = this.dockerClient.listContainersCmd().exec();
        LOG.info("Found {} containers", containers.size());
        dockerSubmodel.addSubmodelEntry("Containers", containers, Container::getId);

        var images = this.dockerClient.listImagesCmd().exec();
        LOG.info("Found {} images", images.size());
        dockerSubmodel.addSubmodelEntry("Images", images, Image::getId);

        var networks = this.dockerClient.listNetworksCmd().exec();
        LOG.info("Found {} networks", networks.size());
        dockerSubmodel.addSubmodelEntry("Networks", networks, Network::getName);

        var volumes = this.dockerClient.listVolumesCmd().exec().getVolumes();
        LOG.info("Found {} volumes", volumes.size());
        dockerSubmodel.addSubmodelEntry("Volumes", volumes, InspectVolumeResponse::getName);

        try {
            var services = this.dockerClient.listServicesCmd().exec();
            dockerSubmodel.addSubmodelEntry("Services", services, Service::getId);

            var tasks = this.dockerClient.listTasksCmd().exec();
            dockerSubmodel.addSubmodelEntry("Tasks", tasks, Task::getName);

            var swarmNodes = this.dockerClient.listSwarmNodesCmd().exec();
            dockerSubmodel.addSubmodelEntry("Swarm Nodes", swarmNodes, SwarmNode::getId);

            var configs = this.dockerClient.listConfigsCmd().exec();
            dockerSubmodel.addSubmodelEntry("Configs", configs, Config::getId);

            var secrets = this.dockerClient.listSecretsCmd().exec();
            dockerSubmodel.addSubmodelEntry("Secrets", secrets, Secret::getId);
        } catch (DockerException exception) {
            LOG.info("Docker runs not in Swarm mode ");
        }

        return List.of(dockerSubmodel);
    }

    @Override
    public List<String> getSubmodelIds() {
        try {
            return this.getModelsByGenericCode().stream().map(Identifiable::getId).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return List.of();
        }
    }

    @Override
    public Optional<Submodel> getSubmodelById(String id) throws IOException {
        var models = this.getModelsByGenericCode();
        for (Submodel model : models) {
            if (model.getId().equals(id)) {
                return Optional.of(model);
            }
        }
        return Optional.empty();
    }

}
