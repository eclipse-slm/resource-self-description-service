package org.eclipse.slm.self_description_service.templating;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.eclipse.slm.self_description_service.templating.method.CapitalizeMethod;
import org.eclipse.slm.self_description_service.templating.method.IndexOfMethod;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Component
public class TemplateRenderer {


    private Configuration cfg;
    private Map<String, Object> globalRenderContext = new HashMap<>();


    public TemplateRenderer() {
        /* ------------------------------------------------------------------------ */
        /* You should do this ONLY ONCE in the whole application life-cycle:        */

        /* Create and adjust the configuration singleton */
        cfg = new Configuration(Configuration.VERSION_2_3_33);
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

    public String render(String template, Map<String, Object> renderContext) {
        try {
            var combinedRenderContext = new HashMap<>(globalRenderContext);
            combinedRenderContext.putAll(renderContext);

            combinedRenderContext.put("capitalize", new CapitalizeMethod());
            combinedRenderContext.put("indexOf", new IndexOfMethod());

            var t = new Template("", new StringReader(template), cfg);

            StringWriter writer = new StringWriter();
            t.process(combinedRenderContext, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends Map<String, Object>> T render(T map) {
        return this.render(map, this.globalRenderContext);
    }

    public <T extends Map<String, Object>> T render(T map, Map<String, Object> renderContext) {
        map.replaceAll((k, v) -> {
            var renderedValue = this.render(v.toString(), renderContext);
            return renderedValue;
        });

        return map;
    }
}