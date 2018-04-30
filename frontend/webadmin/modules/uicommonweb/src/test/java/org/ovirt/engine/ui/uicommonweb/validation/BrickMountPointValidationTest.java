package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetupExtension;

@SuppressWarnings("HardcodedFileSeparator")
@ExtendWith(UiCommonSetupExtension.class)
public class BrickMountPointValidationTest {
    private BrickMountPointValidation validation;

    @BeforeEach
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
