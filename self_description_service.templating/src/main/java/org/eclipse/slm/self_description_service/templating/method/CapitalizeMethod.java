package org.eclipse.slm.self_description_service.templating.method;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;

public class CapitalizeMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Wrong arguments number");
        }

        String arg = arguments.get(0).toString();
        return arg.substring(0, 1).toUpperCase() + arg.substring(1);
    }
}