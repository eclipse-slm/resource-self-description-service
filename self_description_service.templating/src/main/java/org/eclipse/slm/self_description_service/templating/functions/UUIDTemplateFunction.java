package org.eclipse.slm.self_description_service.templating.functions;

import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UUIDTemplateFunction extends AbstractTemplateFunctions {

    public static final String NAMESPACE = "uuid";

    public UUIDTemplateFunction() {
        try {
            var generateUUIDMethodRef = UUIDTemplateFunction.class.getDeclaredMethod("generateUUID");
            var generateUUIDTemplateMethod = new ELFunctionDefinition(
                    UUIDTemplateFunction.NAMESPACE, "generate", generateUUIDMethodRef);
            this.templateFunctions.add(generateUUIDTemplateMethod);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

}