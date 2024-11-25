package org.eclipse.slm.self_description_service.templating.functions;

import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;

public class NowFunction extends AbstractTemplateFunctions{

    private static final String NAMESPACE = "test";


    public NowFunction() {
        try {
            var getValueOfPropertyMethodRef = NowFunction.class.getDeclaredMethod(
                    "now");

            var nowFunction = new ELFunctionDefinition(
                    NAMESPACE, "now", getValueOfPropertyMethodRef);

            this.templateFunctions.add(nowFunction);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String now() {
        var value = String.valueOf(System.currentTimeMillis());
        return value;
    }

}
