package org.eclipse.slm.self_description_service.templating.method;

import org.eclipse.slm.self_description_service.templating.TemplateRenderer;
import org.eclipse.slm.self_description_service.templating.method.utils.PathHelper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;




interface GetResult{
    String getExpectedResult();
}

class TemplateTest{
    public HashMap<String, Object> data;
    public String testTemplate;

    public GetResult getResult;

    public TemplateTest(HashMap<String, Object> data, String testTemplate, GetResult expectedResult) {
        this.data = data;
        this.testTemplate = testTemplate;
        this.getResult = expectedResult;
    }

    public TemplateTest(String testTemplate, GetResult expectedResult) {
        this.testTemplate = testTemplate;
        this.getResult = expectedResult;
        this.data = new HashMap<>();
    }
}

public class TemplateRendererTests {

    private String getPathForFile(String fileName) throws URISyntaxException {
        URL res = getClass().getClassLoader().getResource(fileName);
        File file = Paths.get(res.toURI()).toFile();
        String absolutePath = file.getAbsolutePath().replace("\\", "\\\\");
        return absolutePath;
    }

    @Test
    public void RenderTest() throws URISyntaxException {
        var templateRenderer = new TemplateRenderer();

        var osIsWindows = System.getProperty("os.name").toLowerCase().contains("windows");

        var tests = List.of(
                new TemplateTest(
                        "${.now?date?iso_utc}",
                        () -> {
                            var formatter = new SimpleDateFormat("yyyy-MM-dd");
                            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                            return formatter.format(new Date());
                        }
                ),
                new TemplateTest(
                        "<#assign x = \"something\">${x?indexOf(\"met\")}" ,
                        () -> {
                            return "2";
                        }
                ),
                new TemplateTest(
                        "${indexOf(\"met\", \"something\")}",
                        () -> {
                            return "2";
                        }
                ),
                new TemplateTest(
                        "${capitalize(\"hello\")}" ,
                        () -> {
                            return "Hello";
                        }
                ),
                new TemplateTest(
                        String.format("${JsonFileValue(\"%s\", \"%s\")?string[\"0.###\"]}", "$.['price range'].cheap",
                                PathHelper.getPathForFile(this, "json/simple_file.json", osIsWindows)),
                        () -> {
                            return "10.123";
                        }
                ),
                new TemplateTest(
                        String.format("${YamlFileValue(\"%s\", \"%s\")?string[\"0.0\"]}", "$.['price range'].cheap",
                                PathHelper.getPathForFile(this, "yaml/simple_file.yaml", osIsWindows)),
                        () -> {
                            return "10.0";
                        }
                )
        );


        for (var test : tests) {
            var result = templateRenderer.render(test.testTemplate, test.data);
            var expectedResult = test.getResult.getExpectedResult();
            assertEquals(expectedResult, result);
        }


    }

}
