package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates that the specific string does not contain UTF characters.
 */
public class NonUtfValidation extends BaseI18NValidation {

    public NonUtfValidation() {
        super(ConstantsManager.getInstance().getConstants().nonUtfValidationMsg());
        setIsNegate(true);
    }

    @Override
    protected String composeRegex() {
        return "[" + utfLettersWithoutAsciiLetters() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
