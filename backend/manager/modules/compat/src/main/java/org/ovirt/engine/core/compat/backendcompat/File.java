package org.ovirt.engine.core.compat.backendcompat;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Date;

import org.ovirt.engine.core.compat.CompatException;

public class File {

    public static boolean Exists(String filename) {
        java.io.File file = new java.io.File(filename);
        return file.exists();
    }

    public static void Delete(String filename) {
        try {
            java.io.File file = new java.io.File(filename);
            file.delete();
        } catch (Exception e) {
            throw new CompatException(e);
        }

    }

    public static StreamReaderCompat OpenText(String fileName) {
        try {
            java.io.File file = new java.io.File(fileName);
            FileReader fr = new FileReader(file);
            return new StreamReaderCompat(fr);
        } catch (Exception e) {
            throw new CompatException(e);
        }
    }

    public static String ReadAllText(String filename) {
        try {
            java.io.File file = new java.io.File(filename.toString());
            FileInputStream fis = new FileInputStream(file);
            int size = fis.available();
            byte[] contents = new byte[size];
            fis.read(contents);
            return new String(contents);
        } catch (Exception e) {
            throw new CompatException(e);
        }
    }

    public static Date GetLastWriteTime(Object filename) {
        try {
            java.io.File file = new java.io.File(filename.toString());
            return new Date(file.lastModified());
        } catch (Exception e) {
            throw new CompatException(e);
        }
    }

}
