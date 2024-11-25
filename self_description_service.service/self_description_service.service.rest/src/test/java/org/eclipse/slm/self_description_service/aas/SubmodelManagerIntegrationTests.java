package org.eclipse.slm.self_description_service.aas;

import mock.TestTemplateManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
public class SubmodelManagerIntegrationTests {

    @Autowired
    private SubmodelManager manager;


    @Test
    public void getSubmodels_Success() throws IOException {
        var templateManager = new TestTemplateManager();
        manager.setTemplateManager(templateManager);

        var submodels = manager.getSubmodels();

        assertNotNull(submodels);
        assertFalse(submodels.isEmpty());


    }

}
