package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates that the specific string does not contain UTF characters.
 */
public class NonUtfValidation extends BaseI18NValidation {

    public NonUtfValidation() {
        setIsNegate(true);
    }

    @Override
    protected String composeRegex() {
        return "[" + allUtfLetters() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String asciiLetters() {
        // exclude ascii letters
        return ""; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().nonUtfValidationMsg();
    }

}
