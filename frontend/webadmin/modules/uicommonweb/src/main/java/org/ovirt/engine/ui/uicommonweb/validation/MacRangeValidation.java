package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class MacRangeValidation implements IValidation {

    private final String lowestMacAddress;

    public MacRangeValidation(String lowestMacAddress) {
        this.lowestMacAddress = lowestMacAddress;
    }

    public static long macToLong(String mac) {
        final int HEX_RADIX = 16;

        String macWithoutCommas = mac.replaceAll(":", "");  //$NON-NLS-1$ //$NON-NLS-2$
        return Long.parseLong(macWithoutCommas, HEX_RADIX);
    }

    @Override
    public ValidationResult validate(Object value) {
        String highestMacAddress = (String) value;

        if (highestMacAddress.compareToIgnoreCase(lowestMacAddress) < 0) {
            ValidationResult res = new ValidationResult();
            res.setSuccess(false);
            res.getReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .invalidMacRangeRightBound());
            return res;
        }

        long highestMacAddressLong = macToLong(highestMacAddress);
        long lowerMacAddressLong = macToLong(lowestMacAddress);
        long macCount = highestMacAddressLong - lowerMacAddressLong + 1;
        if (macCount >= Integer.MAX_VALUE) {
            ValidationResult res = new ValidationResult();
            res.setSuccess(false);
            res.getReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .tooBigMacRange());
            return res;
        }

        return new ValidationResult();
    }

}
