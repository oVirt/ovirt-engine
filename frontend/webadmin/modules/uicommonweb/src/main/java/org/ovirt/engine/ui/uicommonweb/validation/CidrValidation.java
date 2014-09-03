package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.Arrays;

import org.ovirt.engine.core.common.validation.CidrValidator;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class CidrValidation implements IValidation {

    public CidrValidation() {
    }

    @Override
    public ValidationResult validate(Object value) {
        // This validation must be applied to a String
        assert value == null || value instanceof String;
        String cidr = (String) value;
        ValidationResult result = new ValidationResult();
        if (!CidrValidator.getInstance().isCidrFormatValid(cidr)) {
            failWith(result, getThisFieldMustContainCidrInFormatMsg());
        } else if (!CidrValidator.getInstance().isCidrNetworkAddressValid(cidr)) {
            failWith(result, getCidrNotNetworkAddress());
        }

        return result;
    }

    private void failWith(ValidationResult result, String message) {
        result.setSuccess(false);
        result.setReasons(Arrays.asList(message));
        return;
    }

    protected String getThisFieldMustContainCidrInFormatMsg() {
        return ConstantsManager.getInstance()
                .getConstants()
                .thisFieldMustContainCidrInFormatMsg();
    }

    protected String getCidrNotNetworkAddress() {
        return ConstantsManager.getInstance().getConstants().cidrNotNetworkAddress();
    }

}
