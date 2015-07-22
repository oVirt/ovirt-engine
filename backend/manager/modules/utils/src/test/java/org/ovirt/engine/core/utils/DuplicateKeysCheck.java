package org.ovirt.engine.core.utils;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class DuplicateKeysCheck {
    public static File loadFileFromPath(String relativePath) {
        String baseDir =  System.getProperty("basedir");
        assumeNotNull(baseDir);

        return new File(baseDir, relativePath);
    }

    public static void assertNoDuplicateKeys(File file) throws IOException {
        NoDuplicateProperties props = new NoDuplicateProperties();
        try (InputStream is = new FileInputStream(file)) {
            props.load(is);
        }
        catch (DuplicatePropertyException exception) {
            fail("Check for duplicate keys in " + file.getAbsolutePath() + " failed: " + exception.getMessage());
        }
    }
}
