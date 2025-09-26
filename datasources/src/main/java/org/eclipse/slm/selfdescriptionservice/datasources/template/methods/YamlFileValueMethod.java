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

        File initialFile = new File(path);
        try {
            InputStream targetFile = new FileInputStream(initialFile);
            var yamlMapper = new ObjectMapper(new YAMLFactory());
            var jsonNode = yamlMapper.readTree(targetFile);
            var jsonMapper = new ObjectMapper();
            var jsonString = jsonMapper.writeValueAsString(jsonNode);
            var json = JsonPath.parse(jsonString);

            var valuePath = list.get(0).toString();
            return json.read(valuePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
