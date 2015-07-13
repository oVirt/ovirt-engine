package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class IpAddressValidation extends RegexValidation {
    public static final String IP_ADDRESS_REGEX =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"; //$NON-NLS-1$
    public IpAddressValidation() {
        this(false);
    }

    public IpAddressValidation(boolean allowEmpty) {
        String empty = ""; //$NON-NLS-1$
        if (allowEmpty) {
            empty = "|^$"; //$NON-NLS-1$
            setMessage(ConstantsManager.getInstance().getConstants().emptyOrValidIPaddressInFormatMsg());
        } else {
            setMessage(ConstantsManager.getInstance().getConstants().thisFieldMustContainIPaddressInFormatMsg());
        }
        setExpression("^" + IP_ADDRESS_REGEX + "$" + empty); //$NON-NLS-1$ $NON-NLS-2$

    }
}
