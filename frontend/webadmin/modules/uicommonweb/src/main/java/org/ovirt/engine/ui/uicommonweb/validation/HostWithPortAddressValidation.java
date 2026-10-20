package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates a host address with an optional port, e.g. {@code host.example.com:8443}, {@code 192.0.2.1:8443} or
 * {@code [2001:db8::1]:8443}. Anything accepted by {@link HostAddressValidation}, including a bare IPv6 address, is
 * accepted as well. A protocol prefix is not allowed.
 */
public class HostWithPortAddressValidation extends BaseI18NValidation {

    private final HostAddressValidation hostAddressValidation;

    public HostWithPortAddressValidation() {
        this(ConstantsManager.getInstance().getConstants().hostAddressWithOptionalPortInvalidReason());
    }

    HostWithPortAddressValidation(String message) {
        super(message);
        hostAddressValidation = new HostAddressValidation(message);
    }

    @Override
    protected String composeRegex() {
        return start() + ValidationUtils.HOSTNAME_FOR_URI + port() + end();
    }

    private String port() {
        return "(:[0-9]{1,5})?"; //$NON-NLS-1$
    }

    @Override
    public ValidationResult validate(Object value) {
        if (hostAddressValidation.validate(value).getSuccess()) {
            return ValidationResult.ok();
        }
        return super.validate(value);
    }
}
