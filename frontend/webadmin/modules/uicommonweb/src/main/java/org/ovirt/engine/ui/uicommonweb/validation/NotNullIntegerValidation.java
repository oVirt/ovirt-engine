package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validation for Integer value that cannot be null - for example to validate results of
 * {@link org.ovirt.engine.ui.common.widget.parser.generic.ToIntEntityModelParser}
 */
public class NotNullIntegerValidation extends IntegerValidation {
    public NotNullIntegerValidation() {
    }

    public NotNullIntegerValidation(int min, int max) {
        super(min, max);
    }

    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            final String reason = ConstantsManager.getInstance().getConstants().thisFieldMustContainIntegerNumberInvalidReason();
            return ValidationResult.fail(reason);
        } else {
            return super.validate(value);
        }
    }
}
