package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.ValidatorConstraint.ValidatorConstraintArgsFormatValue;
import org.ovirt.engine.core.common.validation.annotation.Mask;

public class MaskConstraint implements ConstraintValidator<Mask, String> {

    @Override
    public void initialize(Mask constraintAnnotation) {
    }

    @Override
    public boolean isValid(String mask, ConstraintValidatorContext context) {
        if (mask == null || mask.isEmpty()) {
            return true;
        }

        boolean maskFormatValidation = isMaskFormatValid(mask);
        ValidatorConstraintArgsFormatValue args =
                new ValidatorConstraintArgsFormatValue(maskFormatValidation,
                        maskFormatValidation ? isMaskValid(mask) : false,
                        EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name(),
                        EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name());
        return ValidatorConstraint.getInstance().isValid(args, context, "mask");//$NON-NLS-1$
    }

    private boolean isMaskFormatValid(String mask) {
        return getMaskValidator().isPrefixValid(mask) || getMaskValidator().isValidNetmaskFormat(mask);
    }

    private boolean isMaskValid(String mask) {
        return getMaskValidator().isPrefixValid(mask) || getMaskValidator().isNetmaskValid(mask);
    }

    IPv4MaskValidator getMaskValidator() {
        return IPv4MaskValidator.getInstance();
    }

}
