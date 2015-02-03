package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class MacAddressValidation extends RegexValidation {
    public MacAddressValidation() {
        setExpression("^[a-fA-F0-9]{2}(:[a-fA-F0-9]{2}){5}$"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().invalidMacAddressMsg());
    }
}
