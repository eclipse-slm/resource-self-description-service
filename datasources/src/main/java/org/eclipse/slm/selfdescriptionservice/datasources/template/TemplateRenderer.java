package org.eclipse.slm.selfdescriptionservice.datasources.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.eclipse.slm.selfdescriptionservice.datasources.template.datasourcevalues.DataSourceValueRegistry;
import org.eclipse.slm.selfdescriptionservice.datasources.template.datasourcevalues.DataSourceValueScalarModel;
import org.eclipse.slm.selfdescriptionservice.datasources.template.methods.CommandValueMethod;
import org.eclipse.slm.selfdescriptionservice.datasources.template.methods.JsonFileValueMethod;
import org.eclipse.slm.selfdescriptionservice.datasources.template.methods.YamlFileValueMethod;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Precomputed context with all DataSourceValues grouped by prefix (e.g. docker.version -> docker.version and docker.version).
     */
    private final Map<String, Object> dataSourceValueContext;

    @Autowired
    public TemplateRenderer(DataSourceValueRegistry dataSourceValueRegistry) {
        cfg = new Configuration(Configuration.VERSION_2_3_0);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        var dataSourceValuesContext = new HashMap<String, Object>();
        // Add all registered DataSourceValues as TemplateScalarModel
        for (var entry : dataSourceValueRegistry.getAll().entrySet()) {
            dataSourceValuesContext.put(entry.getKey(), new DataSourceValueScalarModel(entry.getValue()));
        }
        // Generic grouping: Add all DataSourceValues with prefix (e.g. docker.version) as sub-maps
        Map<String, Map<String, Object>> groupedMaps = new HashMap<>();
        for (var entry : dataSourceValueRegistry.getAll().entrySet()) {
            String key = entry.getKey();
            int dotIdx = key.indexOf('.');
            if (dotIdx > 0 && dotIdx < key.length() - 1) {
                String prefix = key.substring(0, dotIdx);
                String suffix = key.substring(dotIdx + 1);
                groupedMaps.computeIfAbsent(prefix, k -> new HashMap<>())
                    .put(suffix, new DataSourceValueScalarModel(entry.getValue()));
            }
        }
        dataSourceValuesContext.putAll(groupedMaps);
        this.dataSourceValueContext = Map.copyOf(dataSourceValuesContext);
    }

    public String render(String template) {
        return this.render(template, this.globalRenderContext);
    }

    public String render(String templateContent, Map<String, Object> renderContext) {
        try {
            var combinedRenderContext = new HashMap<>(globalRenderContext);
            combinedRenderContext.putAll(renderContext);
            combinedRenderContext.putAll(dataSourceValueContext);
            combinedRenderContext.put("JsonFileValue", new JsonFileValueMethod());
            combinedRenderContext.put("YamlFileValue", new YamlFileValueMethod());
            combinedRenderContext.put("ShellCommand", new CommandValueMethod());
            var template = new Template("", new StringReader(templateContent), cfg);

            StringWriter writer = new StringWriter();
            template.process(combinedRenderContext, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends Map<String, Object>> T render(T map, Map<String, Object> renderContext) {
        map.replaceAll((k, v) -> this.render(v.toString(), renderContext));
        return map;
    }
}