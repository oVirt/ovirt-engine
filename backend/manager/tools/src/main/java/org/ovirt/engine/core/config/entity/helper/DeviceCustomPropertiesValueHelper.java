package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;

/**
 * Validates device custom properties definition entered through {@code engine-config} tool
 */
public class DeviceCustomPropertiesValueHelper extends StringValueHelper {
    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(ConfigKey key, String value) {
        boolean result = true;
        String errMsg = null;

        if (!DevicePropertiesUtils.getInstance().isDevicePropertiesDefinitionValid(value)) {
            result = false;
            errMsg =
                    "Invalid syntax, custom device properties specification should conform to "
                            + DevicePropertiesUtils.getInstance().getDevicePropertiesDefinition();
        }

        return new ValidationResult(result, errMsg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpNote(ConfigKey key) {
        return "Custom device properties specification should conform to "
                + DevicePropertiesUtils.getInstance().getDevicePropertiesDefinition();
    }
}
