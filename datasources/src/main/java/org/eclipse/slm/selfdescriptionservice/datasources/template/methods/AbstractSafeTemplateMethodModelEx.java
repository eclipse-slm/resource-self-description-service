package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import freemarker.template.TemplateMethodModelEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Base class for safe TemplateMethodModelEx implementations.
 * Catches all exceptions and returns a generic error string for Freemarker.
 */
public abstract class AbstractSafeTemplateMethodModelEx implements TemplateMethodModelEx {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractSafeTemplateMethodModelEx.class);

    @Override
    public Object exec(List arguments) {
        try {
            return safeExec(arguments);
        } catch (Exception e) {
            LOG.error("Error in TemplateMethodModelEx '{}': {}", getClass().getSimpleName(), e.getMessage());
            LOG.debug("Stacktrace:", e);
            return "Error during template rendering, see service log for error details";
        }
    }

    protected abstract Object safeExec(List arguments) throws Exception;
}

