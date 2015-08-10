package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.SizeFromConfigValue;

public class SizeFromConfigValueValidator implements ConstraintValidator<SizeFromConfigValue, Integer> {

    private Integer min = Integer.MIN_VALUE;

    private Integer max = Integer.MAX_VALUE;

    @Override
    public void initialize(SizeFromConfigValue constraintAnnotation) {
        if (constraintAnnotation.minConfig() != ConfigValues.Invalid) {
            min = Config.<Integer>getValue(constraintAnnotation.minConfig());
        }

        if (constraintAnnotation.maxConfig() != ConfigValues.Invalid) {
            max = Config.<Integer>getValue(constraintAnnotation.maxConfig());
        }

        if (constraintAnnotation.min() != -1) {
            min = constraintAnnotation.min();
        }

        if (constraintAnnotation.max() != -1) {
            max = constraintAnnotation.max();
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value > max || value < min) {
            return false;
        }

        return true;
    }
}
