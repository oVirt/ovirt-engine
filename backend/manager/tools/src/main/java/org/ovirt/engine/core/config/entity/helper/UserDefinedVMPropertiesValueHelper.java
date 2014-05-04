package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.config.entity.ConfigKey;

/**
 * Validates user defined VM properties definition entered through engine-config tool
 */
public class UserDefinedVMPropertiesValueHelper extends StringValueHelper {
    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(ConfigKey key, String value) {
        boolean result = true;
        String errMsg = null;

        if (VmPropertiesUtils.getInstance().syntaxErrorInProperties(value)) {
            result = false;
            errMsg =
                    "Invalid syntax, user defined VM properties specification should conform to "
                            + VmPropertiesUtils.getInstance().getVmPropSpec();
        }
        return new ValidationResult(result, errMsg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpNote(ConfigKey key) {
        return "User defined VM properties specification should conform to "
                + VmPropertiesUtils.getInstance().getVmPropSpec();
    }
}
