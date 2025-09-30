package config;

import mock.TestTemplateManager;
import org.eclipse.slm.selfdescriptionservice.datasources.DatasourceRegistry;
import org.eclipse.slm.selfdescriptionservice.datasources.template.ITemplateManager;
import org.eclipse.slm.selfdescriptionservice.datasources.template.TemplateDatasource;
import org.eclipse.slm.selfdescriptionservice.datasources.template.TemplateRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Optional;

@Configuration
public class TestConfig {

    @Primary
    @Bean
    public ITemplateManager getTemplateManager() {
        return new TestTemplateManager();
    }

    @Bean
    public TemplateRenderer templateRenderer() {
        return new TemplateRenderer(new DatasourceRegistry(), Optional.empty());
    }

    @Bean
    public TemplateDatasource templateDatasource(ITemplateManager templateManager, TemplateRenderer templateRenderer) {
        return new TemplateDatasource(
                new DatasourceRegistry(),
                "test-resource-id",
                true,
                true,
                templateManager,
                templateRenderer,
                List.of()
        );
    }


}
