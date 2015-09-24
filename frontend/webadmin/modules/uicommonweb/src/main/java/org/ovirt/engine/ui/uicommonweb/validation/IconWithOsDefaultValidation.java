package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicommonweb.models.vms.IconWithOsDefault;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class IconWithOsDefaultValidation implements IValidation {

    @Override
    public ValidationResult validate(Object value) {
        if (!(value instanceof IconWithOsDefault)) {
            throw new IllegalArgumentException("Illegal argument type: " //$NON-NLS-1$
                    + (value == null ? "null" : value.getClass().toString())); //$NON-NLS-1$
        }
        final IconWithOsDefault iconWithOsDefault = (IconWithOsDefault) value;
        if (iconWithOsDefault.getValidationResult() == null) {
            UIConstants constants = ConstantsManager.getInstance().getConstants();
            return ValidationResult.fail(constants.iconNotValidatedYet());
        }
        return iconWithOsDefault.getValidationResult();
    }
}
