package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SpecialAsciiI18NOrNoneValidation extends BaseI18NValidation {

    @Override
    protected String composeRegex() {
        return "^[\u0000-\u007F" + allUtfLetters() + "]*$"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().specialAsciiI18NOrNoneValidationMsg();
    }

}
