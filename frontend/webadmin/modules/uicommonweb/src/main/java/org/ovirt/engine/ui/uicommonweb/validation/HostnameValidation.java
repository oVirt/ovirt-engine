package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates a hostname for length and allowable characters
 */
public class HostnameValidation implements IValidation {
    private static final String nameRegex = "^[-_\\.0-9a-zA-Z]*$"; //$NON-NLS-1$
    private static final String nameMessage = ConstantsManager.getInstance().getConstants().hostNameValidationMsg();
    private static final int maxLength = 255;

    private static final RegexValidation regexValidator = new RegexValidation(nameRegex, nameMessage);
    private static final NotEmptyValidation notEmptyValidator = new NotEmptyValidation();
    private static final LengthValidation lengthValidator = new LengthValidation(maxLength);

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult notEmptyValidation = notEmptyValidator.validate(value);
        if (!notEmptyValidation.getSuccess()) {
            return notEmptyValidation;
        }

        ValidationResult lengthValidation = lengthValidator.validate(value);
        if (!lengthValidation.getSuccess()) {
            return lengthValidation;
        }

        return regexValidator.validate(value);
    }
}


