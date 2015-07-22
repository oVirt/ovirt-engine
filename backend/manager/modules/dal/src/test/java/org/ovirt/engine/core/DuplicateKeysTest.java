package org.ovirt.engine.core;

import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.ovirt.engine.core.utils.DuplicateKeysCheck;

public class DuplicateKeysTest {
    @Test
    public void testDuplicateKeys() throws IOException {
        String baseDir = System.getProperty("basedir");
        assumeNotNull(baseDir);

        File file = new File(baseDir, "src/main/resources/bundles/AppErrors.properties");
        DuplicateKeysCheck.assertNoDuplicateKeys(file.getAbsolutePath());
    }
}
