package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidUri;

public class UriConstraint implements ConstraintValidator<ValidUri, String> {

    @Override
    public void initialize(ValidUri constraintAnnotation) {
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return name == null || ValidationUtils.validUri(name);
    }

}
