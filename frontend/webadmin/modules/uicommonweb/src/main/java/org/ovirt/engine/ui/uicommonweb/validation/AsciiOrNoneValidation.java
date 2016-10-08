package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class AsciiOrNoneValidation extends BaseI18NValidation {

    public AsciiOrNoneValidation() {
        super(ConstantsManager.getInstance().getConstants().asciiOrNoneValidationMsg());
    }

    @Override
    protected String composeRegex() {
        return "^[\u0000-\u007F]*$"; //$NON-NLS-1$
    }
}
