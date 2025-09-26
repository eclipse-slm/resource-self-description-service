package org.eclipse.slm.selfdescriptionservice.datasources.template.datasourcevalues;

import freemarker.template.TemplateScalarModel;
import org.eclipse.slm.selfdescriptionservice.datasources.docker.DataSourceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for DataSourceValue to expose it as a TemplateScalarModel for Freemarker.
 * Returns error message in case of exception during value retrieval.
 */
public class DataSourceValueScalarModel implements TemplateScalarModel {
    private final static Logger LOG = LoggerFactory.getLogger(DataSourceValueScalarModel.class);

    private final DataSourceValue<?> value;

    public DataSourceValueScalarModel(DataSourceValue<?> value) {
        this.value = value;
    }

    @Override
    public String getAsString() {
        try {
            return String.valueOf(value.getValue());
        } catch (Exception e) {
            LOG.error("Error rendering DataSourceValue '{}': {}", value.getKey(), e.getMessage(), e);
            return "Error during template rendering, see service log for error details";
        }
    }
}