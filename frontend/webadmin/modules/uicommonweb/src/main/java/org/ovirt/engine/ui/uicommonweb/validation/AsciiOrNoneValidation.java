package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class AsciiOrNoneValidation extends SpecialAsciiI18NOrNoneValidation {

    @Override
    protected String allUtfLetters() {
        // UTF is not allowed
        return ""; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().asciiOrNoneValidationMsg();
    }

}
