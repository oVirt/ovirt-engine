package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

public class ConfiguredRangeValidator implements ConstraintValidator<ConfiguredRange, Integer> {

    private int min;
    private int max;
    private String rangeMessage;

    @Override
    public void initialize(ConfiguredRange constraintAnnotation) {
        if (constraintAnnotation.minConfigValue() != ConfigValues.Invalid) {
            min = Config.<Integer> getValue(constraintAnnotation.minConfigValue(), ConfigCommon.defaultConfigurationVersion);
        } else {
            min = constraintAnnotation.min();
        }

        max = Config.<Integer> getValue(constraintAnnotation.maxConfigValue(), ConfigCommon.defaultConfigurationVersion);
        /* this is to interpolate "${range}" in Validates */
        rangeMessage = "$range " + min + "-" + max;

    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        boolean result = value == null ? true : value >= min && value <= max;
        if (!result) {
            /* this will add a constraint message with interpolated variable for can do actions */
            context.buildConstraintViolationWithTemplate(rangeMessage).addConstraintViolation();
        }
        return result;
    }
    }
