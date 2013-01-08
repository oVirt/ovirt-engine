package org.ovirt.engine.ui.userportal;

import org.junit.Test;
import org.ovirt.engine.core.utils.DuplicateKeysCheck;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assume.assumeNotNull;

public class DuplicateKeysTest {
       @Test
       public void testkDuplicateKeys() throws FileNotFoundException {
          String baseDir =  System.getProperty("basedir"); //$NON-NLS-1$
          assumeNotNull(baseDir);
          String fileName = "AppErrors.properties"; //$NON-NLS-1$
          File file = new File(baseDir + "/src/main/resources/org/ovirt/engine/ui/frontend/" + fileName);  //$NON-NLS-1$
          DuplicateKeysCheck.assertNoDuplicateKeys(file.getAbsolutePath());
       }

}
