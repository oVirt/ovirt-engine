package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class CidrValidation extends RegexValidation {

    public CidrValidation() {
        setExpression("^" + IpAddressValidation.IP_ADDRESS_REGEX + "(?:/(?:3[0-2]|[12]?[0-9]))$"); //$NON-NLS-1$ $NON-NLS-2$
    }

    @Override
    public String getMessage() {
        return ConstantsManager.getInstance().getConstants().thisFieldMustContainCidrInFormatMsg();
    }
}
