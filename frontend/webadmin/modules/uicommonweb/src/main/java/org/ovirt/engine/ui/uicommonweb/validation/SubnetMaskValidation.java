package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.Arrays;

import org.ovirt.engine.core.common.validation.IPv4MaskValidator;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SubnetMaskValidation implements IValidation {
    private final boolean isPrefixAllowed;

    public SubnetMaskValidation() {
        this.isPrefixAllowed = false;
    }

    public SubnetMaskValidation(boolean isPrefixSupported) {
        this.isPrefixAllowed = isPrefixSupported;
    }

    protected String getSubnetBadFormatMessage() {
        return isPrefixAllowed ? getBadPrefixOrNetmaskFormatMessage() : getBadNetmaskFormatMessage();

    }

    protected String getBadPrefixOrNetmaskFormatMessage() {
        return ConstantsManager.getInstance()
                .getConstants()
                .thisFieldMustContainValidPrefixOrNetmask();
    }

    protected String getBadNetmaskFormatMessage() {
        return ConstantsManager.getInstance()
                .getConstants()
                .thisFieldMustContainValidNetmask();
    }

    protected String getInvalidMask() {
        return ConstantsManager.getInstance().getConstants().inValidNetmask();
    }

    @Override
    public ValidationResult validate(Object value) {
        assert value == null || value instanceof String : "This validation must be run on a String!";//$NON-NLS-1$
        String mask = (String) value;

        if (getMaskValidator().isValidNetmaskFormat(mask)) {
            if (!getMaskValidator().isNetmaskValid(mask)) {
                return failWith(getInvalidMask());
            }
        } else {
            if (isPrefixAllowed) {
                if (!getMaskValidator().isPrefixValid(mask)) {
                    return failWith(getBadPrefixOrNetmaskFormatMessage());
                }
            } else {
                return failWith(getBadNetmaskFormatMessage());
            }

        }

        return new ValidationResult();
    }

    private ValidationResult failWith(String errorMessage) {
        return new ValidationResult(false, Arrays.asList(errorMessage));
    }

    IPv4MaskValidator getMaskValidator() {
        return IPv4MaskValidator.getInstance();
    }

}
