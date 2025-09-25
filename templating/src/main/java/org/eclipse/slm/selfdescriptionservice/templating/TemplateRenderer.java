package org.eclipse.slm.selfdescriptionservice.templating;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.eclipse.slm.selfdescriptionservice.templating.method.JsonFileValueMethod;
import org.eclipse.slm.selfdescriptionservice.templating.method.YamlFileValueMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Component
public class TemplateRenderer {


    private final Configuration cfg;
    private final Map<String, Object> globalRenderContext = new HashMap<>();


    public TemplateRenderer() {
        /* ------------------------------------------------------------------------ */
        /* You should do this ONLY ONCE in the whole application life-cycle:        */

        /* Create and adjust the configuration singleton */
        cfg = new Configuration(Configuration.VERSION_2_3_0);
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        /* ------------------------------------------------------------------------ */
        /* You usually do these for MULTIPLE TIMES in the application life-cycle:   */
    }

    public String render(String template) {
        return this.render(template, this.globalRenderContext);
    }

    public String render(String templateContent, Map<String, Object> renderContext) {
        try {
            var combinedRenderContext = new HashMap<>(globalRenderContext);
            combinedRenderContext.putAll(renderContext);

            combinedRenderContext.put("JsonFileValue", new JsonFileValueMethod());
            combinedRenderContext.put("YamlFileValue", new YamlFileValueMethod());

            var template = new Template("", new StringReader(templateContent), cfg);

            StringWriter writer = new StringWriter();
            template.process(combinedRenderContext, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends Map<String, Object>> T render(T map) {
        return this.render(map, this.globalRenderContext);
    }

    public <T extends Map<String, Object>> T render(T map, Map<String, Object> renderContext) {
        map.replaceAll((k, v) -> this.render(v.toString(), renderContext));

        return map;
    }
}