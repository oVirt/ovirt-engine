package org.ovirt.engine.core;

import java.io.IOException;

import org.junit.Test;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.PropertiesTestUtils;

public class ErrorMessagesTest {
    @Test
    public void testDuplicateKeys() throws IOException {
        PropertiesTestUtils.assertNoDuplicateKeys
                (PropertiesTestUtils.loadFileFromPath("src/main/resources/bundles/AppErrors.properties"));
    }

    @Test
    public void testRedundantMessages() throws IOException {
        PropertiesTestUtils.assertNoRedundantKeys
                (PropertiesTestUtils.loadFileFromPath("src/main/resources/bundles/AppErrors.properties"),
                        EngineMessage.class, EngineError.class);
    }
}
