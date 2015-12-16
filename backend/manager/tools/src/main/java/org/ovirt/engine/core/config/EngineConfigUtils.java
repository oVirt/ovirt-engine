package org.ovirt.engine.core.config;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.lang.StringUtils;

public class EngineConfigUtils {

    /**
     * @return The first file found from the list
     */
    public static File locateFileInPaths(String... paths) throws FileNotFoundException {
        for (String path : paths) {
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    return file;
                }
            }
        }
        String msg = "Files " + StringUtils.join(paths, ",\n") + " does not exist";
        throw new FileNotFoundException(msg);
    }
}
