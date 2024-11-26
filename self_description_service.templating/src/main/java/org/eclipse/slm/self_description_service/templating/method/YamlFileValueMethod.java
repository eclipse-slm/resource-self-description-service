package org.eclipse.slm.self_description_service.templating.method;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class YamlFileValueMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List list) throws TemplateModelException {

        if (list.size() != 2) {
            throw new TemplateModelException("Wrong number of arguments");
        }

        var path = list.get(1).toString();

        File initialFile = new File(path);
        try {
            InputStream targetFile = new FileInputStream(initialFile);
            var yamlMapper = new ObjectMapper(new YAMLFactory());
            var jsonNode = yamlMapper.readTree(targetFile);
            var jsonMapper = new ObjectMapper();
            var jsonString = jsonMapper.writeValueAsString(jsonNode);
            var json = JsonPath.parse(jsonString);

            var valuePath = list.get(0).toString();
            return json.read(valuePath).toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
