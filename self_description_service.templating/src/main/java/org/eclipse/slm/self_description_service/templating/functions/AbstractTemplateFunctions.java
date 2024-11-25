package org.eclipse.slm.self_description_service.templating.functions;

import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;

import java.util.ArrayList;
import java.util.List;

public class AbstractTemplateFunctions {

    protected List<ELFunctionDefinition> templateFunctions = new ArrayList<>();

    public List<ELFunctionDefinition> getTemplateFunctions() {
        return templateFunctions;
    }

}
