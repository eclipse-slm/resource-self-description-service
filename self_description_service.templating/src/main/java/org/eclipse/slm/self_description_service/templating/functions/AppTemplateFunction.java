package org.eclipse.slm.self_description_service.templating.functions;

import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppTemplateFunction extends AbstractTemplateFunctions {

    private static final String NAMESPACE = "app";

    private final Environment environment;

    public AppTemplateFunction
            (Environment environment) {
        this.environment = environment;

        try {
            var getValueOfPropertyMethodRef = AppTemplateFunction.class.getDeclaredMethod(
                    "getValueOfProperty",
                    String.class);

            var getAppPropTemplateFunction = new ELFunctionDefinition(
                    UUIDTemplateFunction.NAMESPACE, "generate", getValueOfPropertyMethodRef);

//            var getAppPropTemplateFunction = InjectedContextFunctionProxy.defineProxy(
//                    AppTemplateFunctions.NAMESPACE, "prop",
//                    getValueOfPropertyMethodRef, this);
            this.templateFunctions.add(getAppPropTemplateFunction);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValueOfProperty(String propertyPath) {
        var value = this.environment.getProperty(propertyPath);
        return value;
    }

}