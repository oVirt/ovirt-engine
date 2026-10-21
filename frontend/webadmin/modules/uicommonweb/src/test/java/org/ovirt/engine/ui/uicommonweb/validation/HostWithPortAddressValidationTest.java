package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HostWithPortAddressValidationTest {

    private final HostWithPortAddressValidation validation = new HostWithPortAddressValidation("");

    @ParameterizedTest
    @ValueSource(strings = {
        "vcenter", //$NON-NLS-1$
        "vcenter.example.com", //$NON-NLS-1$
        "vcenter.example.com:1", //$NON-NLS-1$
        "vcenter.example.com:43543", //$NON-NLS-1$
        "192.0.2.1", //$NON-NLS-1$
        "192.0.2.1:443", //$NON-NLS-1$
        "2001:db8::1", //$NON-NLS-1$
        "[2001:db8::1]", //$NON-NLS-1$
        "[2001:db8::1]:8443" //$NON-NLS-1$
    })
    public void valid(String address) {
        assertTrue(validation.validate(address).getSuccess(), address);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "", //$NON-NLS-1$
        "vcenter.example.com:", //$NON-NLS-1$
        "vcenter.example.com:abc", //$NON-NLS-1$
        "vcenter.example.com:123456", //$NON-NLS-1$
        "https://vcenter.example.com", //$NON-NLS-1$
        "https://vcenter.example.com:443", //$NON-NLS-1$
        "vcenter.example.com/path", //$NON-NLS-1$
        "vcen ter.example.com", //$NON-NLS-1$
        "2001:db8::1:8443:" //$NON-NLS-1$
    })
    public void invalid(String address) {
        assertFalse(validation.validate(address).getSuccess(), address);
    }
}
