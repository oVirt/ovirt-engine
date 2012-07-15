package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

public class ConfiguredRangeValidator implements ConstraintValidator<ConfiguredRange, Integer> {

    private int min;
    private int max;

    @Override
    public void initialize(ConfiguredRange constraintAnnotation) {
        if (constraintAnnotation.minConfigValue() != ConfigValues.Invalid) {
            min = Config.<Integer> GetValue(constraintAnnotation.minConfigValue(), Config.DefaultConfigurationVersion);
        } else {
            min = constraintAnnotation.min();
        }

        max = Config.<Integer> GetValue(constraintAnnotation.maxConfigValue(), Config.DefaultConfigurationVersion);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null ? true : value >= min && value <= max;
    }
}
