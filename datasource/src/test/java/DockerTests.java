import org.eclipse.slm.self_description_service.datasource.docker.DockerDatasourceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class DockerTests {
    private final DockerDatasourceService dockerDatasourceServiceDatasource = new DockerDatasourceService("docker");

    public DockerTests() {
    }

    @Test
    void GetSubmodelsSuccessful() {
        var models = this.dockerDatasourceServiceDatasource.getModels();

        assertFalse(models.isEmpty());
    }


}
