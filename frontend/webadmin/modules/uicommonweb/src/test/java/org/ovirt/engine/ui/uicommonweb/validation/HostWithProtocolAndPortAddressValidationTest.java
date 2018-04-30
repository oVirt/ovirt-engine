package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HostWithProtocolAndPortAddressValidationTest {

    private final HostWithProtocolAndPortAddressValidation validation =
            new HostWithProtocolAndPortAddressValidation("");

    @Test
    public void onlyHostname() {
        assertTrue(validation.validate("someHostname").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void hostnameWithProtocol() {
        assertTrue(validation.validate("Xasd://someHostname").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void hostnameShortPort() {
        assertTrue(validation.validate("someHostname:1").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void hostnameNormalPort() {
        assertTrue(validation.validate("someHostname:4040").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void hostnameLongPort() {
        assertTrue(validation.validate("someHostname:65535").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void fullCorrect() {
        assertTrue(validation.validate("someProtocol://someHostname:4040").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void fullCorrectWithIpv4() {
        assertTrue(validation.validate("someProtocol://1.2.3.4:666").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void fullCorrectWithIpv6() {
        assertTrue(validation.validate("someProtocol://[1:2:3:4:5:6:7:8]:666").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void hostnameTooLongPort() {
        assertFalse(validation.validate("someHostname:655359").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void incorrect() {
        assertFalse(validation.validate("someHostname:").getSuccess()); //$NON-NLS-1$
        assertFalse(validation.validate("://someHostname").getSuccess()); //$NON-NLS-1$
        assertFalse(validation.validate("so m eHostname").getSuccess()); //$NON-NLS-1$
        assertFalse(validation.validate("asd:/someHostname").getSuccess()); //$NON-NLS-1$
        assertFalse(validation.validate("asd:someHostname").getSuccess()); //$NON-NLS-1$
        assertFalse(validation.validate("someHostname:abc").getSuccess()); //$NON-NLS-1$
    }
}
