import org.eclipse.slm.selfdescriptionservice.datasources.template.TemplateManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateManagerTests {

    private TemplateManager templateManager = new TemplateManager();

    @Test
    public void Get_Templates_Successfully() throws IOException {
        var templates = templateManager.getTemplates();
        assertThat(templates).isNotEmpty();
    }

    @Test
    public void Get_Template_Successfully() throws IOException {
        var templates = templateManager.getTemplates();
        assertThat(templates).isNotEmpty();

        var fileName = templates[0].getFilename();
        var templateOption = templateManager.getTemplate(fileName);
        assertThat(templateOption).isPresent();
        var template = templateOption.get();
        assertThat(template.getFilename()).isEqualTo(fileName);

    }
}
