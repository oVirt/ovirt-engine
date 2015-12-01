package org.ovirt.engine.core.common.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.validation.annotation.NoRepetitiveStaticIpInList;

public class NoRepetitiveStaticIpInListConstraint implements ConstraintValidator<NoRepetitiveStaticIpInList, List<VdsNetworkInterface>> {

    @Override
    public boolean isValid(List<VdsNetworkInterface> value, ConstraintValidatorContext context) {
        Collection<String> staticIps = new HashSet<>();
        for (VdsNetworkInterface networkInterface : value) {
            String address = networkInterface.getAddress();
            if (networkInterface.getBootProtocol() == NetworkBootProtocol.STATIC_IP
                    && address != null && !address.isEmpty()) {
                if (staticIps.contains(networkInterface.getAddress())) {
                    return false;
                } else {
                    staticIps.add(networkInterface.getAddress());
                }
            }
        }
        return true;
    }

    @Override
    public void initialize(NoRepetitiveStaticIpInList constraintAnnotation) {
        // Unimplemented method, required for interface ConstraintValidator
    }
}
