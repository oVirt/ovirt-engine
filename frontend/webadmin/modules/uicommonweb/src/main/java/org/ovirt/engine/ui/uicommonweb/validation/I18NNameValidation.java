package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates a string that should contain only alphanumeric characters (in any UTF language, including RTL languages),
 * numbers, '_' or '-' sign.
 * <p>
 * The \p{L} can not be used as the JavaScript implementation of the RegExp does not support it.
 */
public class I18NNameValidation extends BaseI18NValidation {

    @Override
    protected String composeRegex() {
        return start() + letters() + numbers() + specialCharacters() + end();
    }

    protected String start() {
        return "^["; //$NON-NLS-1$
    }

    protected String end() {
        return "]+$"; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().i18NNameValidationMsg();
    }

}
