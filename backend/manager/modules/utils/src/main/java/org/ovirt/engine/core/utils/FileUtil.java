package org.ovirt.engine.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtil {
    public static String readAllText(final String filename) throws IOException {
        FileInputStream fis = null;
        try {
            File file = new File(filename.toString());
            fis = new FileInputStream(file);
            int size = fis.available();
            byte[] contents = new byte[size];
            fis.read(contents);
            return new String(contents);
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            // in the absence of commons io, this workaround is needed to close the file
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
