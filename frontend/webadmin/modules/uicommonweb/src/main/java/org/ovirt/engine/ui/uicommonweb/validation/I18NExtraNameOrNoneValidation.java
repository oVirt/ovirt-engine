package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates a string that should contain only alphanumeric characters (in any UTF language, including RTL languages),
 * numbers, '_',  '-' or '+' sign.
 * <p>
 * The \p{L} can not be used as the JavaScript implementation of the RegExp does not support it.
 */
public class I18NExtraNameOrNoneValidation extends I18NNameValidation {

    public I18NExtraNameOrNoneValidation(String message) {
        super(message);
    }

    public I18NExtraNameOrNoneValidation() {
        super(ConstantsManager.getInstance().getConstants().i18NExtraNameOrNoneValidation());
    }

    @Override
    protected String specialCharacters() {
        return "\\+" + super.specialCharacters(); //$NON-NLS-1$
    }

    @Override
    protected String end() {
        return "]*$"; //$NON-NLS-1$
    }
}
