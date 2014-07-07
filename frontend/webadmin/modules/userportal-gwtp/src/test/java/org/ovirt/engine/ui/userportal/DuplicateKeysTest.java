package org.ovirt.engine.ui.userportal;

import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.ovirt.engine.core.utils.DuplicateKeysCheck;

public class DuplicateKeysTest {
       @SuppressWarnings("NonJREEmulationClassesInClientCode")
       @Test
       public void testDuplicateKeys() throws FileNotFoundException {
          String baseDir =  System.getProperty("basedir"); //$NON-NLS-1$
          assumeNotNull(baseDir);

          File file = new File(baseDir, "src/main/resources/org/ovirt/engine/ui/frontend/AppErrors.properties");  //$NON-NLS-1$
          DuplicateKeysCheck.assertNoDuplicateKeys(file.getAbsolutePath());
       }
}
