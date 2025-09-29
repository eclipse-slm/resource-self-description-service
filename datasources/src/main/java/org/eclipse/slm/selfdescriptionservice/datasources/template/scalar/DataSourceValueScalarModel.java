package org.eclipse.slm.selfdescriptionservice.datasources.template.scalar;

import freemarker.template.TemplateScalarModel;
import org.eclipse.slm.selfdescriptionservice.datasources.base.DataSourceValueDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for DataSourceValue to expose it as a TemplateScalarModel for Freemarker.
 * Returns error message in case of exception during value retrieval.
 */
public class DataSourceValueScalarModel implements TemplateScalarModel {
    private final static Logger LOG = LoggerFactory.getLogger(DataSourceValueScalarModel.class);

    private final DataSourceValueDefinition<?> value;

    public DataSourceValueScalarModel(DataSourceValueDefinition<?> value) {
        this.value = value;
    }

    @Override
    public String getAsString() {
        try {
            return String.valueOf(value.getValue());
        } catch (Exception e) {
            LOG.error("Error rendering DataSourceValue '{}': {}", value.getKey(), e.getMessage());
            LOG.debug("Stacktrace:", e);
            return "Error during template rendering, see service log for error details";
        }
    }
}