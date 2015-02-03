package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class UnicastMacAddressValidation extends RegexValidation {
    public UnicastMacAddressValidation() {
        setExpression("^[a-fA-F0-9][02468aAcCeE](:[a-fA-F0-9]{2}){5}$"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().invalidUnicastMacAddressMsg());
    }
}
