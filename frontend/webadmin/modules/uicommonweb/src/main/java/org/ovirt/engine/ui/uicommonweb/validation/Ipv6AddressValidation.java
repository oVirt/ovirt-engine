package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class Ipv6AddressValidation extends RegexValidation {

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
        setExpression(start() + ValidationUtils.IPV6_PATTERN + end() + empty);

    }
}
