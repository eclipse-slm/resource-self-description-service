package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Base class for safe TemplateMethodModelEx implementations.
 * Catches all exceptions and returns a generic error string for Freemarker.
 */
public abstract class AbstractSafeTemplateMethodModelEx implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        try {
            return safeExec(arguments);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Error in TemplateMethodModelEx '{}': {}", getClass().getSimpleName(), e.getMessage(), e);
            return "Error, see log";
        }
    }

    protected abstract Object safeExec(List arguments) throws Exception;
}

