package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostAddressValidation extends BaseI18NValidation {

    private final boolean acceptEmptyInput;
    private final boolean supportIpv6;

    public HostAddressValidation(boolean acceptEmptyInput, boolean supportIpv6) {
        this.acceptEmptyInput = acceptEmptyInput;
        this.supportIpv6 = supportIpv6;

        // BaseI18NValidation c'tor calls composeRegex() prior the members of this class are initiailized.
        // Thus it has to be called here again.
        setExpression(composeRegex());
    }

    public HostAddressValidation() {
        this(false, true);
    }

    @Override
    public ValidationResult validate(Object value) {
        if (acceptEmptyInput && (value == null || (value instanceof String && value.equals("")))) {
            return new ValidationResult();
        }
        return super.validate(value instanceof String ? ((String) value).trim() : value);
    }

    @Override
    protected String composeRegex() {
        return start() + hostnameOrIp() + end();
    }

    protected String hostnameOrIp() {
        return "(?:" + ip() + "|" + fqdn() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private String ip() {
        if (supportIpv6) {
            return ipv4() + "|" + ipv6(); //$NON-NLS-1$
        } else {
            return ipv4();
        }
    }

    private String ipv4() {
        return "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"; //$NON-NLS-1$
    }

    private String ipv6() {
        return ValidationUtils.IPV6_PATTERN;
    }

    protected String fqdn() {
        return "([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*"; //$NON-NLS-1$
    }

    protected String start() {
        return "^"; //$NON-NLS-1$
    }

    protected String end() {
        return "$"; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().addressIsNotValidHostNameOrIpAddressInvalidReason();
    }

}
