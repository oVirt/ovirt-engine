package org.ovirt.engine.core;

import java.io.IOException;

import org.junit.Test;
import org.ovirt.engine.core.utils.DuplicateKeysCheck;

public class DuplicateKeysTest {
    @Test
    public void testDuplicateKeys() throws IOException {
        DuplicateKeysCheck.assertNoDuplicateKeys
                (DuplicateKeysCheck.loadFileFromPath("src/main/resources/bundles/AppErrors.properties"));
    }
}
