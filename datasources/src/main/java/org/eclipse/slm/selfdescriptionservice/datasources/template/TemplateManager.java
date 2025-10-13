package org.eclipse.slm.selfdescriptionservice.datasources.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Component
public class TemplateManager implements ITemplateManager {

    private final static Logger LOG = LoggerFactory.getLogger(TemplateManager.class);


    private final String templatePath;

    public TemplateManager(@Value("${datasources.templates.templates-path}") String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public Resource[] getTemplates() throws IOException {
        LOG.info("Using template path: {}", templatePath);

        if (templatePath.startsWith("classpath:")) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            var resources = resolver.getResources(this.templatePath);
            return Arrays.stream(resources)
                    .filter(resource -> Objects.requireNonNull(resource.getFilename()).endsWith(".aasx"))
                    .toArray(Resource[]::new);
        } else {
            File dir = new File(templatePath);
            if (!dir.exists() || !dir.isDirectory()) {
                return new Resource[0];
            }
            return Arrays.stream(Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".aasx"))))
                    .map(FileSystemResource::new)
                    .toArray(Resource[]::new);
        }
    }

    @Override
    public Optional<Resource> getTemplate(String name) throws IOException {
        if (templatePath.startsWith("classpath:")) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            var resources = resolver.getResources(this.templatePath);
            return Arrays.stream(resources)
                    .filter(resource -> Objects.requireNonNull(resource.getFilename()).equals(name))
                    .findFirst();
        } else {
            File dir = new File(templatePath);
            if (!dir.exists() || !dir.isDirectory()) {
                return Optional.empty();
            }
            File file = new File(dir, name);
            if (file.exists() && file.isFile() && file.getName().endsWith(".aasx")) {
                return Optional.of(new FileSystemResource(file));
            } else {
                return Optional.empty();
            }
        }
    }
}
