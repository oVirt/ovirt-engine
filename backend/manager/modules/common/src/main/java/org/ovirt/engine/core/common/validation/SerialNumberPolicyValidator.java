package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.HasSerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.validation.annotation.ValidSerialNumberPolicy;
import org.ovirt.engine.core.compat.StringHelper;

/** Validates serialNumberPolicy with respect to customSerialNumber */
public class SerialNumberPolicyValidator implements ConstraintValidator<ValidSerialNumberPolicy, HasSerialNumberPolicy> {
    @Override
    public void initialize(ValidSerialNumberPolicy constraintAnnotation) {

    }

    @Override
    public boolean isValid(HasSerialNumberPolicy value, ConstraintValidatorContext context) {
        // only incorrect combination is when policy=CUSTOM && customSerialNumber is null or empty
        // other policies ignore the customSerialNumber value
        return value.getSerialNumberPolicy() != SerialNumberPolicy.CUSTOM || !StringHelper.isNullOrEmpty(value.getCustomSerialNumber());
    }
}
