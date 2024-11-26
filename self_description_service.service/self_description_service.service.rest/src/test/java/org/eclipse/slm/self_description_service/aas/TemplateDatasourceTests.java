package org.eclipse.slm.self_description_service.aas;

import mock.TestTemplateManager;
import org.eclipse.slm.self_description_service.aas.factories.TemplateDatasource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
public class TemplateDatasourceTests {

    @Autowired
    private TemplateDatasource datasource;


    @Test
    public void getSubmodels_Success() throws IOException {
        var templateManager = new TestTemplateManager();

        datasource.setTemplateManager(templateManager);

        var submodels = datasource.getModels();

        assertNotNull(submodels);
        assertFalse(submodels.isEmpty());


    }

}
