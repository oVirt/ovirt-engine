package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class DoubleValidation implements IValidation {
    private UIConstants constants = ConstantsManager.getInstance().getConstants();

    @Override
    public ValidationResult validate(Object value) {
        if (!(value instanceof String || value instanceof Double)) {
            return ValidationResult.fail(constants.thisFieldMustContainNumberInvalidReason());
        }

        if (value instanceof String && ((String) value).isEmpty()) {
            return ValidationResult.ok();
        }

        Double doubleValue = value instanceof String ? tryParse((String) value) : (Double) value;
        if (doubleValue == null) {
            return ValidationResult.fail(constants.thisFieldMustContainNumberInvalidReason());
        }

        return ValidationResult.ok();
    }

    private Double tryParse(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
