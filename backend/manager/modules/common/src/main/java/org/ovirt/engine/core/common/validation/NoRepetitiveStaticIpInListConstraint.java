package org.ovirt.engine.core.common.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.NoRepetitiveStaticIpInList;

public class NoRepetitiveStaticIpInListConstraint implements ConstraintValidator<NoRepetitiveStaticIpInList, List<VdsNetworkInterface>> {

    @Override
    public boolean isValid(List<VdsNetworkInterface> value, ConstraintValidatorContext context) {
        Collection<String> staticIps = new HashSet<String>();
        for (VdsNetworkInterface networkInterface : value) {
            if (networkInterface.getBootProtocol() == NetworkBootProtocol.StaticIp
                    && validAddress(networkInterface.getAddress())) {
                if (staticIps.contains(networkInterface.getAddress())) {
                    return false;
                } else {
                    staticIps.add(networkInterface.getAddress());
                }
            }
        }
        return true;
    }

    private boolean validAddress(String address) {
        boolean isValid = false;
        if (address != null) {
            Pattern pattern = Pattern.compile(ValidationUtils.IP_PATTERN);
            Matcher matcher = pattern.matcher(address);
            isValid = matcher.matches();
        }
        return isValid;
    }

    @Override
    public void initialize(NoRepetitiveStaticIpInList constraintAnnotation) {
        // Unimplemented method, required for interface ConstraintValidator
    }
}
