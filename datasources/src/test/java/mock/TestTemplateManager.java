package mock;

import org.eclipse.slm.selfdescriptionservice.datasources.template.ITemplateManager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class TestTemplateManager implements ITemplateManager {
    @Override
    public Resource[] getTemplates() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        var resources = resolver.getResources("classpath:templates/**");


        return Arrays.stream(resources)
                .filter(resource -> Objects.requireNonNull(resource.getFilename()).endsWith(".aasx"))
                .toArray(Resource[]::new);
    }

    @Override
    public Optional<Resource> getTemplate(String name) {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();


        try {
            Resource[] resources = null;
            resources = resolver.getResources("classpath:templates/**");
            return Arrays.stream(resources)
                    .filter(resource -> Objects.requireNonNull(resource.getFilename()).equals(name)).findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
