package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.NullOrStringContainedInConfigValueList;

public class NullOrStringContainedInConfigValueListConstraint
    extends ContainedInConfigValueListConstraint<NullOrStringContainedInConfigValueList, String> {

    @Override
    public ConfigValues getConfigValue(NullOrStringContainedInConfigValueList constraintAnnotation) {
        return constraintAnnotation.configValue();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || super.isValid(value, context);
    }
}
