package org.eclipse.slm.self_description_service.templating.method;

import freemarker.template.TemplateModelException;
import org.eclipse.slm.self_description_service.templating.utils.PathHelper;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonFileValueMethodTests {


    @Test
    public void ReadValueFromSimpleFile_Successful() throws TemplateModelException, URISyntaxException {

        var jsonFileValueMethod = new JsonFileValueMethod();

        var xpath = "$.['price range'].cheap";
        var filePath = PathHelper.getPathForFile(this, "json/simple_file.json");

        var result = jsonFileValueMethod.exec(List.of(xpath, filePath));

        var assertValue = "10.0";

        assertEquals(assertValue, result);

    }

}
