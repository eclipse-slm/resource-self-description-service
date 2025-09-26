package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import freemarker.template.TemplateModelException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class YamlFileValueMethod extends AbstractSafeTemplateMethodModelEx {
    @Override
    public Object safeExec(List list) throws Exception {

        if (list.size() != 2) {
            throw new TemplateModelException("Wrong number of arguments");
        }

        var path = list.get(1).toString();

        InputStream targetFile = getClass().getClassLoader().getResourceAsStream(path);
        if (targetFile == null) {
            try {
                targetFile = new FileInputStream(path);
            } catch (IOException e) {
                throw new RuntimeException("Resource or file not found: " + path, e);
            }
        }
        var yamlMapper = new ObjectMapper(new YAMLFactory());
        var jsonNode = yamlMapper.readTree(targetFile);
        var jsonMapper = new ObjectMapper();
        var jsonString = jsonMapper.writeValueAsString(jsonNode);
        var json = JsonPath.parse(jsonString);

        var valuePath = list.get(0).toString();
        return JsonPathReader.readSingleValueFromPath(json, valuePath);
    }
}
