package org.eclipse.slm.self_description_service.templating;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;




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

    @Test
    public void RenderTest(){
        var templateRenderer = new TemplateRenderer();

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
                        "${capitalize(\"hello\")}" ,
                        () -> {
                            return "Hello";
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
