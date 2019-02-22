package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.annotation.Cidr;

public class CidrConstraint implements ConstraintValidator<Cidr, ExternalSubnet> {

    @Override
    public void initialize(Cidr constraintAnnotation) {
    }

    @Override
    public boolean isValid(ExternalSubnet subnet, ConstraintValidatorContext context) {
        boolean isIpv4 = subnet.getIpVersion().equals(ExternalSubnet.IpVersion.IPV4);
        if (!isCidrFormatValid(subnet.getCidr(), isIpv4)) {
            return failWith(context, EngineMessage.BAD_CIDR_FORMAT.name());
        }

        if (!isCidrNetworkAddressValid(subnet.getCidr(), isIpv4)) {
            return failWith(context, EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name());
        }

        return true;
    }

    private boolean failWith(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addNode("cidr")
                .addConstraintViolation();
        return false;
    }

    private boolean isCidrFormatValid(String cidr, boolean isIpv4) {
        return getCidrValidator().isCidrFormatValid(cidr, isIpv4);
    }

    private boolean isCidrNetworkAddressValid(String cidr, boolean isIpv4) {
        return getCidrValidator().isCidrNetworkAddressValid(cidr, isIpv4);
    }

    CidrValidator getCidrValidator() {
        return CidrValidator.getInstance();
    }

}
