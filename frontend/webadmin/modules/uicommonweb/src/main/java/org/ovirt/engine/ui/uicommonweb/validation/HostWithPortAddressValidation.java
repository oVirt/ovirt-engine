package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates a host address with an optional port, e.g. {@code host.example.com:8443}, {@code 192.0.2.1:8443} or
 * {@code [2001:db8::1]:8443}. Anything accepted by {@link HostAddressValidation}, including a bare IPv6 address, is
 * accepted as well. A protocol prefix is not allowed.
 */
public class HostWithPortAddressValidation extends HostAddressValidation {

    public HostWithPortAddressValidation() {
        this(ConstantsManager.getInstance().getConstants().hostAddressWithOptionalPortInvalidReason());
    }

    HostWithPortAddressValidation(String message) {
        super(message);
    }

    @Override
    protected String composeRegex() {
        return super.composeRegex() + "|" + start() + ValidationUtils.HOSTNAME_FOR_URI + port() + end(); //$NON-NLS-1$
    }

    private String port() {
        return "(:[0-9]{1,5})?"; //$NON-NLS-1$
    }
}
