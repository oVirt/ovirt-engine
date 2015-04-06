package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostAddressValidation extends BaseI18NValidation {

    private final boolean acceptEmptyInput;

    public HostAddressValidation(boolean acceptEmptyInput) {
        this.acceptEmptyInput = acceptEmptyInput;
    }

    public HostAddressValidation() {
        this(false);
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
        return "(" + ip() + "|" + fqdn() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    protected String ip() {
        return "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"; //$NON-NLS-1$
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
