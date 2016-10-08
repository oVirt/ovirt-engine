package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostAddressValidation extends BaseI18NValidation {

    private final boolean acceptEmptyInput;
    private final boolean supportIpv6;

    public HostAddressValidation(boolean acceptEmptyInput, boolean supportIpv6, String message) {
        super(message);
        this.acceptEmptyInput = acceptEmptyInput;
        this.supportIpv6 = supportIpv6;

        // BaseI18NValidation c'tor calls composeRegex() prior the members of this class are initialized.
        // Thus it has to be called here again.
        setExpression(composeRegex());
    }

    public HostAddressValidation() {
        this(false, true);
    }

    HostAddressValidation(String message) {
        this(false, true, message);
    }

    public HostAddressValidation(boolean acceptEmptyInput, boolean supportIpv6) {
        this(acceptEmptyInput,
                supportIpv6,
                ConstantsManager.getInstance().getConstants().addressIsNotValidHostNameOrIpAddressInvalidReason());
    }

    @Override
    public ValidationResult validate(Object value) {
        if (acceptEmptyInput && (value == null || (value instanceof String && value.equals(EMPTY_STRING)))) {
            return new ValidationResult();
        }
        return super.validate(value instanceof String ? ((String) value).trim() : value);
    }

    @Override
    protected String composeRegex() {
        return start() + hostnameOrIp() + end();
    }

    private String hostnameOrIp() {
        return "(?:" + ip() + "|" + ValidationUtils.FQDN_PATTERN + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private String ip() {
        if (supportIpv6) {
            return ValidationUtils.IPV4_PATTERN_NON_EMPTY + "|" + getIpv6Pattern(); //$NON-NLS-1$
        } else {
            return ValidationUtils.IPV4_PATTERN_NON_EMPTY;
        }
    }

    protected String getIpv6Pattern() {
        return ValidationUtils.IPV6_PATTERN;
    }
}
