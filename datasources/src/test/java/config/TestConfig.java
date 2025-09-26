package config;

import mock.TestTemplateManager;
import org.eclipse.slm.selfdescriptionservice.datasources.template.datasourcevalues.DataSourceValueRegistry;
import org.eclipse.slm.selfdescriptionservice.datasources.template.ITemplateManager;
import org.eclipse.slm.selfdescriptionservice.datasources.template.TemplateRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfig {

    @Primary
    @Bean
    public ITemplateManager getTemplateManager() {
        return new TestTemplateManager();
    }

    @Bean
    public TemplateRenderer templateRenderer() {
        return new TemplateRenderer(new DataSourceValueRegistry());
    }


}
