package org.ovirt.engine.core.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

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

    public static String readAllText(final String filename) throws IOException {
        FileInputStream fis = null;
        try {
            java.io.File file = new java.io.File(filename.toString());
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

    public static Date getLastWriteTime(Object filename) {
        try {
            java.io.File file = new java.io.File(filename.toString());
            return new Date(file.lastModified());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the maximum timestamp of directory tree.
     * @param file directory/file name.
     * @return max timestamp.
     */
    public static long getTimestampRecursive(String file) {
        return getTimestampRecursive(new File(file));
    }

    /**
     * Returns the maximum timestamp of directory tree.
     * @param file directory/file name.
     * @return max timestamp.
     */
    public static long getTimestampRecursive(File file) {
        if (file.isDirectory()) {
            long m = 0;
            for (String name : file.list()) {
                m = Math.max(m, getTimestampRecursive(new File(file, name)));
            }
            return m;
        }
        else if (file.isFile()) {
            return file.lastModified();
        }
        else {
            return 0;
        }
    }
}
