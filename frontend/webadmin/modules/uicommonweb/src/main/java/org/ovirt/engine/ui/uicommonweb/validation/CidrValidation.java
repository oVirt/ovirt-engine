package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.Arrays;

import org.ovirt.engine.core.common.validation.CidrValidator;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class CidrValidation implements IValidation {

    public static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = "Argument must be a String or a null"; //$NON-NLS-1$

    private boolean isIpv4;

    public CidrValidation(boolean isIpv4) {
        super();
        this.isIpv4 = isIpv4;
    }

    @Override
    public ValidationResult validate(Object value) {
        // This validation must be applied to a String
        if (!(value instanceof String) && value != null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE);
        }
        String cidr = (String) value;
        ValidationResult result = new ValidationResult();
        if (!getCidrValidator().isCidrFormatValid(cidr, isIpv4)) {
            failWith(result, getThisFieldMustContainCidrInFormatMsg());
        } else if (!getCidrValidator().isCidrNetworkAddressValid(cidr, isIpv4)) {
            failWith(result, getCidrNotNetworkAddress());
        }

        return result;
    }

    private void failWith(ValidationResult result, String message) {
        result.setSuccess(false);
        result.setReasons(Arrays.asList(message));
        return;
    }

    protected UIConstants getUiConstants() {
        return getConstantsManager().getConstants();
    }

    protected ConstantsManager getConstantsManager() {
        return ConstantsManager.getInstance();
    }

    protected String getCidrNotNetworkAddress() {
        return getUiConstants().cidrNotNetworkAddress();
    }

    protected String getThisFieldMustContainCidrInFormatMsg() {
        return getUiConstants()
                .thisFieldMustContainCidrInFormatMsg();
    }

    protected CidrValidator getCidrValidator() {
        return CidrValidator.getInstance();
    }

}
