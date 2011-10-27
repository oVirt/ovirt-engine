package org.ovirt.engine.core.common.validation;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.validation.annotation.IntegerContainedInConfigValueList;

public class IntegerContainedInConfigValueListConstraint implements ConstraintValidator<IntegerContainedInConfigValueList, Integer> {

    private List<String> validValues;

    @Override
    public void initialize(IntegerContainedInConfigValueList constraintAnnotation) {
        validValues = Config.<List<String>> GetValue(constraintAnnotation.configValue());
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null || validValues == null) {
            return false;
        }
        return validValues.contains(value.toString());
    }

}
