import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.eclipse.slm.self_description_service.datasource.docker.DockerDatasourceService;
import org.eclipse.slm.self_description_service.datasource.docker.DockerSubmodel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerTests {
    private final DockerDatasourceService dockerDatasourceServiceDatasource = new DockerDatasourceService("docker");

    public DockerTests() {
    }


    public static GenericContainer nginx;

    static {
        nginx = new GenericContainer(DockerImageName.parse("nginx:1.27.0-alpine3.19-slim")) //
                .withExposedPorts(80).waitingFor(Wait.forHttp("/").forStatusCode(200).forStatusCode(301))
                .waitingFor(new HostPortWaitStrategy());
        nginx.start();
    }

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        Thread.sleep(5000);
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }
    }

    @AfterAll
    public static void afterAll(){
        nginx.stop();
    }


    @Test
    void GetSubmodelsSuccessful() {
        var models = this.dockerDatasourceServiceDatasource.getModels();

        assertThat(models).isNotEmpty();
    }

    @Test
    void SubmodelHasAtLeastOneContainerAndImage() {
        var models = this.dockerDatasourceServiceDatasource.getModels();
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
