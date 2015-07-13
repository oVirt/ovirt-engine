package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NotEmptyValidation implements IValidation {
    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        if (value == null || (value instanceof String && StringHelper.isNullOrEmpty((String) value))) {
            result.setSuccess(false);
            result.getReasons().add(ConstantsManager.getInstance().getConstants().thisFieldCantBeEmptyInvalidReason());
        }

        return result;
    }
}
