package config;

import mock.TestTemplateManager;
import org.eclipse.slm.selfdescriptionservice.datasources.DatasourceService;
import org.eclipse.slm.selfdescriptionservice.datasources.base.Datasource;
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
        return new TemplateRenderer(new DatasourceService(List.of()), Optional.empty());
    }

    @Bean
    public TemplateDatasource templateDatasource(ITemplateManager templateManager, TemplateRenderer templateRenderer) {
        return new TemplateDatasource(
                "test-resource-id",
                true,
                templateManager,
                templateRenderer, 
                List.of()
        );
    }


}
