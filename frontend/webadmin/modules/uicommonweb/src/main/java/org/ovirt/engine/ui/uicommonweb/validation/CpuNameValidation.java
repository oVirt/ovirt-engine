package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Validates a string that should contain only alphanumeric characters (in any UTF language, including RTL languages),
 * numbers, '_',  '-' or '+' or ',' sign.
 */
public class CpuNameValidation extends I18NExtraNameOrNoneValidation {

    public CpuNameValidation() {
        super(ConstantsManager.getInstance().getConstants().cpuNameValidation());
    }

    @Override
    protected String specialCharacters() {
        return "," + super.specialCharacters(); //$NON-NLS-1$
    }

}
