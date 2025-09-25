import config.TestConfig;
import org.eclipse.slm.selfdescriptionservice.datasources.template.TemplateDatasourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TemplateDatasourceService.class, TestConfig.class})
@ComponentScan(basePackages = {"org.eclipse.slm.selfdescriptionservice"})
@TestPropertySource(locations = "classpath:test.yml")
@Import(TestConfig.class)
public class TemplateDatasourceManagerTests {

    @Autowired
    private TemplateDatasourceService datasource;


    @Test
    public void getSubmodels_Success() {

        var submodels = datasource.getSubmodels();

        assertThat(submodels).isNotNull().isNotEmpty();
    }

    @Test
    public void getSubmodels_Ids_Success() {
        var submodelIds = datasource.getMetaDataOfSubmodels();
        assertThat(submodelIds).isNotNull().isNotEmpty();
    }

    @Test
    public void getSubmodel_by_Id_Success() {
        var submodelsMetaData = datasource.getMetaDataOfSubmodels();
        assertThat(submodelsMetaData).isNotNull().isNotEmpty();

        var submodelMetaData = submodelsMetaData.get(0);
        var submodel = datasource.getSubmodelById(submodelMetaData.getId());
        assertThat(submodel).isNotNull().isPresent();

    }

    @Test
    public void getSubmodel_by_Id_Failure() {
        var submodel = datasource.getSubmodelById("");
        assertThat(submodel).isNotPresent();
    }

}
