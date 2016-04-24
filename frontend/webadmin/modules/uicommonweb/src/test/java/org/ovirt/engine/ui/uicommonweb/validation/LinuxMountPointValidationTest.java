package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetup;

@SuppressWarnings("HardcodedFileSeparator")
public class LinuxMountPointValidationTest {
    @ClassRule
    public static final UiCommonSetup setup = new UiCommonSetup();

    private LinuxMountPointValidation validation;

    @Before
    public void setUp() {
        validation = new LinuxMountPointValidation();
    }

    /* Tests */

    @Test
    public void validWithFQDN() {
        assertValid("somehost.somedoamin.com:/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void validWithFQDNNumber() {
        assertValid("somehost.somedoamin.com2:/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void validWithHost() {
        assertValid("somehost:/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void validWithHostNumber() {
        assertValid("somehost2:/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void validWithIpv4() {
        assertValid("1.2.3.4:/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void validWithIpv6() {
        assertValid("[1::2]:/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void invalidJustPath() {
        assertInvalid("/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void invalidColonAndPath() {
        assertInvalid(":/path/to/dir"); // $NON-NLS-1$
    }

    @Test
    public void invalidJustFQDN() {
        assertInvalid("somehost.somedomain.com"); // $NON-NLS-1$
    }

    @Test
    public void invalidColonAndFQDN() {
        assertInvalid("somehost.somedomain.com:"); // $NON-NLS-1$
    }

    @Test
    public void invalidJustHost() {
        assertInvalid("somehost"); // $NON-NLS-1$
    }

    @Test
    public void invalidColonAndHost() {
        assertInvalid("somehost:"); // $NON-NLS-1$
    }

    @Test
    public void invalidJustIP() {
        assertInvalid("1.2.3.4"); // $NON-NLS-1$
    }

    @Test
    public void invalidColonAndIP() {
        assertInvalid("1.2.3.4:"); // $NON-NLS-1$
    }

    @Test
    public void invalidIpv6NotWrappedByBrackets() {
        assertInvalid("1:2:3:4:5:6:7:8:/path/to/dir"); // $NON-NLS-1$
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
