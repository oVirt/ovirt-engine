package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetup;

@SuppressWarnings("HardcodedFileSeparator")
public class BrickMountPointValidationTest {
    @ClassRule
    public static final UiCommonSetup setup = new UiCommonSetup();

    private BrickMountPointValidation validation;

    @Before
    public void setUp() {
        validation = new BrickMountPointValidation();
    }

    @Test
    public void validPath() {
        assertValid("/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void invalidColonAndPath() {
        assertInvalid(":/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void invalidMountPoint() {
        assertInvalid("/.."); // $NON-NLS-1$
        assertInvalid("/."); // $NON-NLS-1$
    }

    /* Helper Methods */

    private void assertValid(String path) {
        assertTrue(pathToBool(path));
    }

    private void assertInvalid(String path) {
        assertFalse(pathToBool(path));
    }

    private boolean pathToBool(String path) {
        return validation.validate(path).getSuccess();
    }
}
