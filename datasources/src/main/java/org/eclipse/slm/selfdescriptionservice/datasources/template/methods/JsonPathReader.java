package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import com.jayway.jsonpath.DocumentContext;

import java.util.List;

public class JsonPathReader {

    public static Object readSingleValueFromPath(DocumentContext context, String path){
        var result = context.read(path);
        if (result instanceof List) {
            return ((List<?>)result).get(0);
        }else{
            return result;
        }
    }

}