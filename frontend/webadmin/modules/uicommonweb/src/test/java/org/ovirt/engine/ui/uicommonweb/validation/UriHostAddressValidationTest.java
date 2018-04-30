package org.ovirt.engine.ui.uicommonweb.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ovirt.engine.ui.uicommonweb.validation.ValidationResult.fail;
import static org.ovirt.engine.ui.uicommonweb.validation.ValidationResult.ok;

import org.junit.jupiter.api.Test;

public class UriHostAddressValidationTest {

    private static final String TEST_VIOLATION_MESSAGE = "test violation message"; //$NON-NLS-1$

    @Test
    public void testEmptyNotAllowedByDefault() {
        doTest("", false); //$NON-NLS-1$
    }

    @Test
    public void testNull() {
        doTest(null, false);
    }

    @Test
    public void testInvalidAddress() {
        doTest("123,abc", false); //$NON-NLS-1$
    }

    @Test
    public void testTrailingSpace() {
        doTest("1.2.3.4 ", true); //$NON-NLS-1$
    }

    @Test
    public void testLeadingSpace() {
        doTest(" 1.2.3.4", true); //$NON-NLS-1$
    }

    @Test
    public void testIpv4Address() {
        doTest("1.2.3.4", true); //$NON-NLS-1$
    }

    @Test
    public void testStdIpv6AddressAllowed() {
        doTest("[1111:2222:3333:4444:0555:6:aaaa:ffff]", true); //$NON-NLS-1$
    }

    @Test
    public void testLeadingZeroesInAnIpv6Block() {
        doTest("[2001:db8::2:0001]", true); //$NON-NLS-1$
    }

    @Test
    public void testCompressed1Ipv6AddressAllowed() {
        doTest("[::]", true); //$NON-NLS-1$
    }

    @Test
    public void testCompressed2Ipv6AddressAllowed() {
        doTest("[1::2]", true); //$NON-NLS-1$
    }

    @Test
    public void testCompressedIpv6AddressWitSubnetPrefix() {
        doTest("::/128", false); //$NON-NLS-1$
    }

    @Test
    public void testHostname() {
        doTest("test.host.name", true); //$NON-NLS-1$
    }

    private void doTest(String value, boolean valid) {
        doTest(value, valid, new UriHostAddressValidation(TEST_VIOLATION_MESSAGE));
    }

    private void doTest(String value, boolean valid, HostAddressValidation underTest) {
        assertThat(underTest.validate(value), is(valid ? ok() : fail(TEST_VIOLATION_MESSAGE)));
    }
}
