package org.eclipse.slm.self_description_service;

import org.springframework.core.io.Resource;

import java.io.IOException;

public interface ITemplateManager {

    Resource[] getTemplates() throws IOException;

}
