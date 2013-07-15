package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NonNegativeLongNumberValidation implements IValidation {

    @Override
    public ValidationResult validate(Object value) {
        Long longValue = null;
        ValidationResult res = new ValidationResult();

        if (value != null && value instanceof String && !((String) value).trim().isEmpty()) { //$NON-NLS-1$
            try {
                longValue = Long.valueOf((String) value);
            } catch (NumberFormatException e) {
                // do nothing, value is already initialized with null
            }

            if (longValue == null || longValue < 0) {
                res.setSuccess(false);
                res.getReasons().add(ConstantsManager.getInstance()
                        .getConstants().thisFieldMustContainNonNegativeIntegerNumberInvalidReason());
            }
        }
        return res;
    }
}
