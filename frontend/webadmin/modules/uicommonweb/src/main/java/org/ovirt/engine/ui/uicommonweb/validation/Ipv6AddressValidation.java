package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class Ipv6AddressValidation extends RegexValidation {
    public static final String IPV6_ADDRESS_REGEX = "(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" //$NON-NLS-1$
            + "((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)"; //$NON-NLS-1$

    public Ipv6AddressValidation() {
        this(false);
    }

    public Ipv6AddressValidation(boolean allowEmpty) {
        String empty = ""; //$NON-NLS-1$
        if (allowEmpty) {
            empty = "|^$"; //$NON-NLS-1$
            setMessage(ConstantsManager.getInstance().getConstants().emptyOrValidIpv6AddressInFormatMsg());
        } else {
            setMessage(ConstantsManager.getInstance().getConstants().thisFieldMustContainIpv6AddressMsg());
        }
        setExpression("^" + IPV6_ADDRESS_REGEX + "$" + empty); //$NON-NLS-1$ $NON-NLS-2$

    }
}
