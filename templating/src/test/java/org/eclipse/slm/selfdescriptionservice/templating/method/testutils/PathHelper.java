package org.eclipse.slm.selfdescriptionservice.templating.method.testutils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class PathHelper {
    public static String getPathForFile(Object object, String fileName, boolean escape) throws URISyntaxException {
        URL res = object.getClass().getClassLoader().getResource(fileName);
        File file = Paths.get(res.toURI()).toFile();
        String absolutePath = file.getAbsolutePath();
        if (escape) {
            absolutePath = absolutePath.replace("\\", "\\\\");
        }
        return absolutePath;
    }

    public static String getPathForFile(Object object, String fileName) throws URISyntaxException {
        return getPathForFile(object, fileName, false);
    }
}
