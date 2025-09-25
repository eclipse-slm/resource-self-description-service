package org.eclipse.slm.selfdescriptionservice.datasources.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.selfdescriptionservice.datasources.AbstractDatasourceService;
import org.eclipse.slm.selfdescriptionservice.datasources.aas.SubmodelMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "datasources.docker.enabled", havingValue = "true", matchIfMissing = false)
public class DockerDatasourceService extends AbstractDatasourceService {

    private final static Logger LOG = LoggerFactory.getLogger(DockerDatasourceService.class);

    public static final String DATASOURCE_NAME = "Docker";

    private DockerClient dockerClient;

    public DockerDatasourceService(@Value("${resource.id}") String resourceId,
                                   @Value("${datasources.docker.docker-host}") String dockerHost) {
        super(resourceId, "Docker");

        LOG.info("Using DOCKER_HOST '{}'", dockerHost);
        var dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(dockerClientConfig, httpClient);
        try {
            dockerClient.pingCmd().exec();
            LOG.info("Connection to Docker Host successful");
        } catch (Exception e) {
            throw new IllegalStateException("Connection to Docker Host not possible. Fix configuration or disable data source using application property " +
                    "'datasources.docker.enabled'. Error was: {}" + e);
        }
    }

    @Override
    public List<Submodel> getSubmodels() {
        var dockerSubmodel = new DockerSubmodel(this.resourceId, this.dockerClient);
        return List.of(dockerSubmodel);
    }

    @Override
    public List<SubmodelMetaData> getMetaDataOfSubmodels() {
        var dockerSubmodelMetaData = DockerSubmodel.getMetaData(this.resourceId);
        return List.of(dockerSubmodelMetaData);
    }

    @Override
    public Optional<Submodel> getSubmodelById(String id) {
        var dockerSubmodel = new DockerSubmodel(this.resourceId, this.dockerClient);
        return Optional.of(dockerSubmodel);
    }

}
