package org.ovirt.engine.core.common.config;

import java.io.File;

public class ConfigUtil {
    /**
     * Given a relative path, this method will resolve the path relative to the supplied directory. If supplied an
     * absolute path, it will be returned unmodified.
     *
     * @param baseDirectory
     *            the base directory for relative paths
     * @param path
     *            the path to be resolved if it is relative
     * @return an absolute path
     */
    public static String resolvePath(String baseDirectory, String path) {
        if (new File(path).isAbsolute()) {
            return path;
        } else {
            return baseDirectory + File.separator + path;
        }
    }
}
