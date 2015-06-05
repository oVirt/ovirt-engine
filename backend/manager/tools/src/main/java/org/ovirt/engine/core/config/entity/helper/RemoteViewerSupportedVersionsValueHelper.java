package org.ovirt.engine.core.config.entity.helper;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class RemoteViewerSupportedVersionsValueHelper extends StringValueHelper {

    public ValidationResult validate(ConfigKey key, String value) {
        if (StringUtils.isEmpty(value)) {
            return new ValidationResult(false, "The provided value can not be empty");
        }

        String[] pairs = value.split(";");
        for (String pair : pairs) {
            String[] nameVersion = pair.split(":");
            if (nameVersion.length != 2 ||
                    StringUtils.isEmpty(nameVersion[0]) ||
                    StringUtils.isEmpty(nameVersion[1])) {
                return new ValidationResult(false, "Valid format is: system1:minVersion;system2:minVersion");
            }
        }

        return new ValidationResult(true);
    }
}
