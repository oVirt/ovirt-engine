package org.ovirt.engine.core.common.validation;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public abstract class ContainedInConfigValueListConstraint<A extends Annotation, T>
    implements ConstraintValidator<A, T> {

    private List<String> validValues;

    @Override
    public void initialize(A constraintAnnotation) {
        validValues = Config.getValue(getConfigValue(constraintAnnotation));
    }

    @Override
    public boolean isValid(T value, ConstraintValidatorContext context) {
        if (value == null || validValues == null) {
            return false;
        }
        return validValues.contains(value.toString());
    }

    public abstract ConfigValues getConfigValue(A constraintAnnotation);
}
