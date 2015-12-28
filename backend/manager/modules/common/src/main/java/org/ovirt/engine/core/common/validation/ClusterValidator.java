package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.validation.annotation.ValidCluster;

public class ClusterValidator implements ConstraintValidator<ValidCluster, Cluster> {

    @Override
    public void initialize(ValidCluster constraintAnnotation) {
    }

    @Override
    public boolean isValid(Cluster value, ConstraintValidatorContext context) {
        if (value.supportsVirtService() && value.getCpuName() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("CLUSTER_CPU_TYPE_CANNOT_BE_NULL")
                    .addNode("cpu_name")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}
