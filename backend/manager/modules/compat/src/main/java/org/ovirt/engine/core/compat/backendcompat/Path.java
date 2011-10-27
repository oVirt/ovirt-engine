package org.ovirt.engine.core.compat.backendcompat;

import java.io.File;
import java.io.IOException;

import org.ovirt.engine.core.compat.CompatException;

public class Path {
    // FIXME Probably needs to be smarter
    public static String Combine(String getDirectoryName, String path) {
        return getDirectoryName + File.separator + path;
    }

    public static String GetDirectoryName(String baseDirectory) {
        return (new File(baseDirectory)).getParent();
    }

    public static synchronized String GetTempFileName() {
        try {
            String prefix = "" + System.currentTimeMillis();
            return File.createTempFile(prefix, ".tmp").getPath();
        } catch (IOException e) {
            throw new CompatException(e);
        }
    }

    public static String GetFileName(String path) {
        return new File(path).getName();
    }

    public static boolean IsPathRooted(String path) {
        return (new File(path)).isAbsolute();
    }
}
