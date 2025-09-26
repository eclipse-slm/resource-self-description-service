package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import com.jayway.jsonpath.JsonPath;
import freemarker.template.TemplateModelException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 *
 */
public class JsonFileValueMethod extends AbstractSafeTemplateMethodModelEx {
    @Override
    public Object safeExec(List list) throws Exception{
        if (list.size() != 2) {
            throw new TemplateModelException("Wrong number of arguments");
        }

        var path = list.get(1).toString();

        File initialFile = new File(path);
        try {
            InputStream targetFile = new FileInputStream(initialFile);
            var jsonFile = JsonPath.parse(targetFile);
            var valuePath = list.get(0).toString();

            return jsonFile.read(valuePath);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
