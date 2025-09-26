package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import com.jayway.jsonpath.JsonPath;
import freemarker.template.TemplateModelException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 *
 */
public class JsonFileValueMethod extends AbstractSafeTemplateMethodModelEx {
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
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Resource or file not found: " + path, e);
            }
        }
        var jsonFile = JsonPath.parse(targetFile);
        var valuePath = list.get(0).toString();
        return JsonPathReader.readSingleValueFromPath(jsonFile, valuePath);
    }
}
