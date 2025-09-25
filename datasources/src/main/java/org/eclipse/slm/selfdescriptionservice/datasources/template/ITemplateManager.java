package org.eclipse.slm.selfdescriptionservice.datasources.template;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Optional;

public interface ITemplateManager {

    Resource[] getTemplates() throws IOException;

    Optional<Resource> getTemplate(String name) throws IOException;
}
