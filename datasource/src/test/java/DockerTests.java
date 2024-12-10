import org.eclipse.slm.self_description_service.datasource.Docker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class DockerTests {
    private final Docker dockerDatasource = new Docker();

    public DockerTests() {
//        this.dockerDatasource = new Docker();
    }

    @Test
    void GetSubmodelsSuccessful() {
        var models = this.dockerDatasource.getModels();

        assertFalse(models.isEmpty());
    }


}
