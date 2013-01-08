package org.ovirt.engine.core;

import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.ovirt.engine.core.utils.DuplicateKeysCheck;

public class DuplicateKeysTest {
    @Test
    public void testkDuplicateKeys() throws FileNotFoundException {
        String baseDir = System.getProperty("basedir");
        assumeNotNull(baseDir);
        String fileName = "AppErrors.properties";
        File file = new File(baseDir + "/src/main/resources/bundles/" + fileName);
        DuplicateKeysCheck.assertNoDuplicateKeys(file.getAbsolutePath());
    }

}
