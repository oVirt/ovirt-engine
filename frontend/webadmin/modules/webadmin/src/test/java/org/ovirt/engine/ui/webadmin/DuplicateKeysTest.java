package org.ovirt.engine.ui.webadmin;

import java.io.IOException;

import org.junit.Test;
import org.ovirt.engine.core.utils.DuplicateKeysCheck;

public class DuplicateKeysTest {
       @SuppressWarnings("NonJREEmulationClassesInClientCode")
       @Test
       public void testDuplicateKeys() throws IOException {
          DuplicateKeysCheck.assertNoDuplicateKeys(DuplicateKeysCheck.loadFileFromPath
                  ("src/main/resources/org/ovirt/engine/ui/frontend/AppErrors.properties")); //$NON-NLS-1$
       }
}
