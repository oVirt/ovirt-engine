package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VmInitNetworkNameValidation extends BaseI18NValidation {

    public VmInitNetworkNameValidation() {
        super(ConstantsManager.getInstance().getConstants().vmInitNetworkNameValidationMsg());
    }

    @Override
    protected String composeRegex() {
        return "^[A-Za-z0-9_:-]*$"; //$NON-NLS-1$;
    }
}
