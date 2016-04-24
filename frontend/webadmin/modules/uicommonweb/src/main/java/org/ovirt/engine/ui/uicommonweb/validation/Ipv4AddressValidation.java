package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class Ipv4AddressValidation extends RegexValidation {
    public Ipv4AddressValidation() {
        this(false);
    }

    public Ipv4AddressValidation(boolean allowEmpty) {
        String empty = ""; //$NON-NLS-1$
        if (allowEmpty) {
            empty = "|^$"; //$NON-NLS-1$
            setMessage(ConstantsManager.getInstance().getConstants().emptyOrValidIpv4AddressInFormatMsg());
        } else {
            setMessage(ConstantsManager.getInstance().getConstants().thisFieldMustContainIpv4AddressInFormatMsg());
        }
        setExpression(start() + ValidationUtils.IPV4_PATTERN_NON_EMPTY + end() + empty);

    }
}
