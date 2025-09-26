package org.eclipse.slm.selfdescriptionservice.datasources.template;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Component
public class TemplateManager implements ITemplateManager {
    @Override
    public Resource[] getTemplates() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        var resources = resolver.getResources("classpath:templates/**");

        return Arrays.stream(resources)
                .filter(resource -> Objects.requireNonNull(resource.getFilename()).endsWith(".aasx"))
                .toArray(Resource[]::new);
    }

    @Override
    public Optional<Resource> getTemplate(String name) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        var resources = resolver.getResources("classpath:templates/**");
        return Arrays.stream(resources)
                .filter(resource -> Objects.requireNonNull(resource.getFilename()).equals(name)).findFirst();
    }
}
