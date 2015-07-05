package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.annotation.Cidr;

public class CidrConstraint implements ConstraintValidator<Cidr, String> {

    @Override
    public void initialize(Cidr constraintAnnotation) {
    }

    @Override
    public boolean isValid(String cidr, ConstraintValidatorContext context) {
        if (!isCidrFormatValid(cidr)) {
            return failWith(context, EngineMessage.BAD_CIDR_FORMAT.name());
        }

        if (!isCidrNetworkAddressValid(cidr)) {
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

    private boolean isCidrFormatValid(String cidr) {
        return getCidrValidator().isCidrFormatValid(cidr);
    }

    private boolean isCidrNetworkAddressValid(String cidr) {
        return getCidrValidator().isCidrNetworkAddressValid(cidr);
    }

    CidrValidator getCidrValidator() {
        return CidrValidator.getInstance();
    }

}
