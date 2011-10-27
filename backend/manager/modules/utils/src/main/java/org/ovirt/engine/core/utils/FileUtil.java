package org.ovirt.engine.core.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    public static InputStream findFile(String name) {
        try {
            InputStream fis = FileUtil.class.getClassLoader().getResourceAsStream(name);
            // If this is null, look on the file system
            if (fis == null) {
                fis = new FileInputStream(name);
            }
            return fis;
        } catch (FileNotFoundException e) {
            throw new VdcException(e);
        }
    }

    public static void copyFile(String srcFilePath, String dstFilePath) throws IOException {
        InputStream in = new FileInputStream(srcFilePath);
        OutputStream out = new FileOutputStream(dstFilePath);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (fileExists(filePath)) {
            file.delete();
        }
    }

    public static void closeQuietly(Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
