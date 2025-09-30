package org.eclipse.slm.selfdescriptionservice.datasources.template;

import org.eclipse.slm.selfdescriptionservice.datasources.DatasourceRegistry;
import org.eclipse.slm.selfdescriptionservice.datasources.template.testutils.PathHelper;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


interface GetResult {
    String getExpectedResult();
}

class TemplateTest {
    public HashMap<String, Object> data;
    public String testTemplate;

    public GetResult getResult;

    public TemplateTest(String testTemplate, GetResult expectedResult) {
        this.testTemplate = testTemplate;
        this.getResult = expectedResult;
        this.data = new HashMap<>();
    }
}

public class TemplateRendererTests {

    @Test
    public void RenderTest() throws URISyntaxException {
        Locale.setDefault(Locale.US);

        var templateRenderer = new TemplateRenderer(new DatasourceRegistry(), Optional.empty());

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
                        "<#assign x = \"something\">${x?indexOf(\"met\")}",
                        () -> "2"
                ),
                new TemplateTest(
                        "${\"hello\"?capitalize}",
                        () -> "Hello"
                ),
                new TemplateTest(
                        String.format("${JsonFileValue(\"%s\", \"%s\")?string[\"0.###\"]}", "$.['price range'].cheap",
                                PathHelper.getPathForFile(this, "json/simple_file.json", osIsWindows)),
                        () -> "10.123"
                ),
                new TemplateTest(
                        String.format("${YamlFileValue(\"%s\", \"%s\")?string[\"0.0\"]}", "$.['price range'].cheap",
                                PathHelper.getPathForFile(this, "yaml/simple_file.yaml", osIsWindows)),
                        () -> "10.1"
                ),
                new TemplateTest(
                        String.format("${ShellCommand(\"%s\", \"%s\", \"%s\")?string[\"0.0\"]}", "echo {'price range':{'cheap':10}}", "json", "$.['price range'].cheap"),
                        () -> "10.0"
                ),
                new TemplateTest(
                        String.format("${ShellCommand('%s', \"%s\", \"%s\")}", "echo \"<root><element>Some data</element></root>\"", "xml", "/root/element"),
                        () -> "Some data"
                ),
                new TemplateTest(
                        String.format("${ShellCommand(\"%s\", \"%s\", \"%s\")}", "echo some element", "regex", "element"),
                        () -> "element"
                )
        );


        for (var test : tests) {
            var result = templateRenderer.render(test.testTemplate, test.data);
            var expectedResult = test.getResult.getExpectedResult();
            assertThat(result).isEqualTo(expectedResult);
        }
    }

}