package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.validation.annotation.NicLabelNicIdOrNicNameIsSet;

public class NicLabelNicIdOrNicNameIsSetConstraint
        implements ConstraintValidator<NicLabelNicIdOrNicNameIsSet, NicLabel> {

    @Override
    public void initialize(NicLabelNicIdOrNicNameIsSet constraintAnnotation) {
    }

    @Override
    public boolean isValid(NicLabel nicLabel, ConstraintValidatorContext context) {
        boolean nicIdNotSet = nicLabel.getNicId() == null;
        boolean nicNameNotSet = nicLabel.getNicName() == null;

        return !(nicIdNotSet && nicNameNotSet);
    }
}
