package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class LengthValidation implements IValidation {
    private int privateMaxLength;

    public int getMaxLength() {
        return privateMaxLength;
    }

    public void setMaxLength(int value) {
        privateMaxLength = value;
    }

    public LengthValidation() {
        setMaxLength(Integer.MAX_VALUE);
    }

    public LengthValidation(int maxLength) {
        setMaxLength(maxLength);
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        if (value != null && value instanceof String && ((String) value).length() > getMaxLength()) {
            result.setSuccess(false);
            result.getReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .lenValidationFieldMusnotExceed(getMaxLength()));
        }

        return result;
    }
}
