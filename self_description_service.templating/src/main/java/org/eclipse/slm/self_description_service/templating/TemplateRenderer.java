package org.eclipse.slm.self_description_service.templating;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import com.hubspot.jinjava.lib.tag.Tag;
import org.eclipse.slm.self_description_service.templating.functions.AppTemplateFunction;
import org.eclipse.slm.self_description_service.templating.functions.NowFunction;
import org.eclipse.slm.self_description_service.templating.functions.UUIDTemplateFunction;
import org.eclipse.slm.self_description_service.templating.tags.TimestampTag;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TemplateRenderer {

    private Jinjava jinjava = new Jinjava();

    private Map<String, Object> globalRenderContext = new HashMap<>();

    private List<Tag> tags = List.of(new TimestampTag());


    public TemplateRenderer(AppTemplateFunction appTemplateFunctions, UUIDTemplateFunction uuidTemplateFunctions) {
        for (var templateFunctionsClass : List.of(
                appTemplateFunctions,
                uuidTemplateFunctions, new NowFunction())
        ) {
            for (var templateFunction : templateFunctionsClass.getTemplateFunctions()) {
                this.jinjava.getGlobalContext().registerFunction(templateFunction);
            }
        }

        for (var tag : tags) {
            this.jinjava.getGlobalContext().registerTag(tag);
        }
    }

    public String render(String template) {
        return this.render(template, this.globalRenderContext);
    }

    public String render(String template, Map<String, Object> renderContext) {
        try {
            var combinedRenderContext = new HashMap<>(globalRenderContext);
            combinedRenderContext.putAll(renderContext);

            var result = this.jinjava.render(template, combinedRenderContext);
            return result;
        } catch (FatalTemplateErrorsException e) {

            e.getErrors().forEach(subError -> {
                subError.getException().printStackTrace();
            });

            throw e;
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
