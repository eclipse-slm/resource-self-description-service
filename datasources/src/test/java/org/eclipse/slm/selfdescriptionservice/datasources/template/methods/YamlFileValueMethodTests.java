package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import freemarker.template.TemplateModelException;
import org.eclipse.slm.selfdescriptionservice.datasources.template.testutils.PathHelper;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlFileValueMethodTests {

    @Test
    public void ReadValueFromSimpleFile_Successful() throws TemplateModelException, URISyntaxException {

        var yamlFileValueMethod = new YamlFileValueMethod();

        var xpath = "$.['price range'].cheap";
        var filePath = PathHelper.getPathForFile(this, "yaml/simple_file.yaml");

        var result = yamlFileValueMethod.exec(List.of(xpath, filePath));

        var assertValue = 10.123123;

        assertThat(result).isEqualTo(assertValue);
    }

    @Test
    public void ReadArrayValueFromSimpleFile_Successful() throws TemplateModelException, URISyntaxException {

        var yamlFileValueMethod = new YamlFileValueMethod();

        var xpath = "$.book[?(@.title=='Beginning JSON')].price";
        var filePath = PathHelper.getPathForFile(this, "json/simple_file.json");

        var result = yamlFileValueMethod.exec(List.of(xpath, filePath));


        var assertValue = 49.99;

        assertThat(result).isEqualTo(assertValue);
    }


}
