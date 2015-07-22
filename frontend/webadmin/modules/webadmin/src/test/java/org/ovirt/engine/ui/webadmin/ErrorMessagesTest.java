package org.ovirt.engine.ui.webadmin;

import java.io.IOException;

import org.junit.Test;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.PropertiesTestUtils;

public class ErrorMessagesTest {
    @Test
    public void testDuplicateKeys() throws IOException {
        PropertiesTestUtils.assertNoDuplicateKeys
                (PropertiesTestUtils.loadFileFromPath("src/main/resources/org/ovirt/engine/ui/frontend/AppErrors.properties")); //$NON-NLS-1$
    }

    @Test
    public void testRedundantMessages() throws IOException {
        PropertiesTestUtils.assertNoRedundantKeys
                (PropertiesTestUtils.loadFileFromPath("src/main/resources/org/ovirt/engine/ui/frontend/AppErrors.properties"),  //$NON-NLS-1$
                        EngineMessage.class, EngineError.class);
    }
}
