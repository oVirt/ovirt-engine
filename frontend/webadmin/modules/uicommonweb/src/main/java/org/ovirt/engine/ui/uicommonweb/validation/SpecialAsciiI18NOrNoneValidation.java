package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SpecialAsciiI18NOrNoneValidation extends BaseI18NValidation {

    public SpecialAsciiI18NOrNoneValidation() {
        super(ConstantsManager.getInstance().getConstants().specialAsciiI18NOrNoneValidationMsg());
    }

    @Override
    protected String composeRegex() {
        return "^[\u0000-\u007F" + allUtfLetters() + "]*$"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
