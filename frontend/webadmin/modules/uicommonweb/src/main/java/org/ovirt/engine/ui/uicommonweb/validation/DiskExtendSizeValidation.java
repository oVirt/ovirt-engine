package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class DiskExtendSizeValidation extends IntegerValidation {
    @Override
    public ValidationResult validate(Object value) {
        Integer intValue = Integer.valueOf((String) value);
        if (intValue == 0) {
            // Valid scenario. Happens when the user wants to:
            // 1) Update alias and/or description.
            //  or
            // 2) Close the dialog with the "OK" button without actually updating any of the fields.
            return ValidationResult.ok();
        }

        if (getMaximum() <= 0) {
            String msg = ConstantsManager.getInstance().getConstants().diskMaxSizeReached();
            return ValidationResult.fail(msg);
        }
        return super.validate(value);
    }
}
