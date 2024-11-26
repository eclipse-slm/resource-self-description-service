package org.eclipse.slm.self_description_service.templating.method;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.List;

public class IndexOfMethod implements TemplateMethodModelEx {

    @Override
    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }

        var t = args.get(0).toString();
        var t2 = args.get(1).toString();

        return new SimpleNumber(
                t2.indexOf(t));
    }

}
