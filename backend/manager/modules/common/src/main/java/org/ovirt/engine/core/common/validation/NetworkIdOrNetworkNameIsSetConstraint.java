package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.validation.annotation.NetworkIdOrNetworkNameIsSet;

public class NetworkIdOrNetworkNameIsSetConstraint
        implements ConstraintValidator<NetworkIdOrNetworkNameIsSet, NetworkAttachment> {

    @Override
    public void initialize(NetworkIdOrNetworkNameIsSet constraintAnnotation) {
    }

    @Override
    public boolean isValid(NetworkAttachment value, ConstraintValidatorContext context) {
        boolean networkIdNotSet = value.getNetworkId() == null;
        boolean networkNameNotSet = value.getNetworkName() == null;

        return !(networkIdNotSet && networkNameNotSet);
    }
}
