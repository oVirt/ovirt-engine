package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates a string that should contain only ASCII letters, numbers, '_' or '-' signs.
 */
public class AsciiNameValidation extends BaseI18NValidation {

    public AsciiNameValidation() {
        super(ConstantsManager.getInstance().getConstants().asciiNameValidationMsg());
    }

    @Override
    protected String composeRegex() {
        return "^[A-Za-z0-9_-]*$"; //$NON-NLS-1$
    }
}
