package org.ovirt.engine.core.utils;

import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class DuplicateKeysCheck {
    public static void assertNoDuplicateKeys(String filePath) throws FileNotFoundException {
        InputStream is = new FileInputStream(filePath);
        NoDuplicateProperties props = new NoDuplicateProperties();
        try {
            props.load(is);
        }
        catch (Exception exception) {
            fail("Check for duplicate keys in " + filePath + " failed: " + exception.getMessage());
        }

    }
}
