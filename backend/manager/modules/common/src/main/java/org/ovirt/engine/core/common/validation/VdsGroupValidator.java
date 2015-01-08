package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.validation.annotation.ValidVdsGroup;

public class VdsGroupValidator implements ConstraintValidator<ValidVdsGroup, VDSGroup> {

    @Override
    public void initialize(ValidVdsGroup constraintAnnotation) {
    }

    @Override
    public boolean isValid(VDSGroup value, ConstraintValidatorContext context) {
        if (value.supportsVirtService() && value.getCpuName() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("VDS_GROUP_CPU_TYPE_CANNOT_BE_NULL")
                    .addNode("cpu_name")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}
