package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class IpAddressValidation extends RegexValidation {

    public IpAddressValidation() {
        this(false);
    }

    public IpAddressValidation(boolean allowEmpty) {
        String empty = ""; //$NON-NLS-1$
        if (allowEmpty) {
            empty = "|^$"; //$NON-NLS-1$
            setMessage(getConstantsManager().getConstants().emptyOrValidIpAddressInFormatMsg());
        } else {
            setMessage(getConstantsManager().getConstants().thisFieldMustContainIpv4OrIpv6AddressMsg());
        }
        setExpression(start() + ValidationUtils.ANY_IP_PATTERN + end() + empty);
    }

    ConstantsManager getConstantsManager() {
        return ConstantsManager.getInstance();
    }
}
