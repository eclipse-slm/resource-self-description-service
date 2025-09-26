package org.eclipse.slm.selfdescriptionservice.datasources.docker;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.eclipse.slm.selfdescriptionservice.datasources.template.datasourcevalues.DataSourceValueRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled()
public class DockerDatasourceTests {

    private static final Logger LOG = LoggerFactory.getLogger(DockerDatasourceTests.class);

    private final DockerDatasourceService dockerDatasourceServiceDatasource =
            new DockerDatasourceService("docker", new DataSourceValueRegistry(), "tcp://localhost:2375");


    public DockerDatasourceTests() {
    }

    public static GenericContainer<?> nginx;

    static {
        nginx = new GenericContainer<>(DockerImageName.parse("nginx:1.27.0-alpine3.19-slim")) //
                .withExposedPorts(80).waitingFor(Wait.forHttp("/").forStatusCode(200).forStatusCode(301));
        nginx.start();
    }

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        Thread.sleep(5000);
    }

    @AfterAll
    public static void afterAll(){
        nginx.stop();
    }


    @Test
    void GetSubmodelsSuccessful() {
        var models = this.dockerDatasourceServiceDatasource.getSubmodels();

        assertThat(models).isNotEmpty();
    }

    @Test
    void SubmodelHasAtLeastOneContainerAndImage() {
        var models = this.dockerDatasourceServiceDatasource.getSubmodels();
        assertThat(models).isNotEmpty();

        Consumer<Optional<?>> checkElement = (elem) -> {
            assertThat(elem).isPresent();
            var containers = (DefaultSubmodelElementList) elem.get();
            assertThat(containers).isNotNull();
            assertThat(containers.getValue()).isNotEmpty();
        };

        var model = (DockerSubmodel) models.get(0);

        var containersOption = model.getContainers();
        checkElement.accept(containersOption);

        var imagesOption = model.getImages();
        checkElement.accept(imagesOption);

        var networks = model.getNetworks();
        checkElement.accept(networks);

        var volumes = model.getVolumes();
        checkElement.accept(volumes);
    }
}
