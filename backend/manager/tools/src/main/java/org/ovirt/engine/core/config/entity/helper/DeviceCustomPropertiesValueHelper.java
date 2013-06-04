package org.ovirt.engine.core.config.entity.helper;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.compat.Version;
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

        if (isCustomDevicePropertiesSupported(key.getVersion())) {
            if (!DevicePropertiesUtils.getInstance().isDevicePropertiesDefinitionValid(value)) {
                result = false;
                errMsg =
                        "Invalid syntax, custom device properties specification should conform to "
                                + DevicePropertiesUtils.getInstance().getDevicePropertiesDefinition();
            }
        } else {
            result = false;
            errMsg = String.format("Device custom properties are not supported in version %s", key.getVersion());
        }
        return new ValidationResult(result, errMsg);
    }

    /**
     * Tests if custom device properties are available in specified cluster version.
     * {@code FeatureSupported.deviceCustomProperties} is not used, because {@code ConfigValues} attributes are not
     * loaded at this time.
     *
     * @param version
     *            specified version
     * @return {@code true} if custom device properties are supported in the version, {@code false} otherwise
     */
    private boolean isCustomDevicePropertiesSupported(String versionStr) {
        boolean result = false;

        if (StringUtils.isNotEmpty(versionStr)) {
            Version version = new Version(versionStr);
            result = version.compareTo(Version.v3_3) >= 0;
        }
        return result;
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
