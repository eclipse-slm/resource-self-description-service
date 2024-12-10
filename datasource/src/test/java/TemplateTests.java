import mock.TestTemplateManager;
import org.eclipse.slm.self_description_service.datasource.Template;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(classes = Template.class)
@TestPropertySource(locations="classpath:test.properties")
public class TemplateTests {

    @Autowired
    private Template datasource;


    @Test
    public void getSubmodels_Success() {
        var templateManager = new TestTemplateManager();

        datasource.setTemplateManager(templateManager);

        var submodels = datasource.getModels();

        assertNotNull(submodels);
        assertFalse(submodels.isEmpty());


    }

}
