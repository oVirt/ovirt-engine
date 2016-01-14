package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.validation.annotation.MTU;

public class MTUValidator implements ConstraintValidator<MTU, Integer> {

    @Override
    public void initialize(MTU constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == 0 || value >= 68;
    }
}
