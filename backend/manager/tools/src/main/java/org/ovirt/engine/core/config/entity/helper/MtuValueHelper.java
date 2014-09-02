package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.common.validation.MTUValidator;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class MtuValueHelper extends IntegerValueHelper {

    @Override
    public ValidationResult validate(ConfigKey key, String value) {

        ValidationResult intValidtionResult = super.validate(key, value);

        if (!intValidtionResult.isOk()) {
            return intValidtionResult;
        }

        MTUValidator mtuValidator = new MTUValidator();
        return mtuValidator.isValid(Integer.parseInt(value), null) ? new ValidationResult(true)
                : new ValidationResult(false, "MTU value is invalid.");
    }

}
