package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class MacRangeValidation implements IValidation {

    private final String lowestMacAddress;

    public MacRangeValidation(String lowestMacAddress) {
        this.lowestMacAddress = lowestMacAddress;
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult res = new ValidationResult();
        if (((String) value).compareToIgnoreCase(lowestMacAddress) < 0) {
            res.setSuccess(false);
            res.getReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .invalidMacRangeRightBound());
        }
        return res;
    }

}
