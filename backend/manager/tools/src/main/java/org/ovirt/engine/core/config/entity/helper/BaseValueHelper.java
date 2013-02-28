package org.ovirt.engine.core.config.entity.helper;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.config.entity.ConfigKey;

public abstract class BaseValueHelper implements ValueHelper {

    protected String getHelpNoteByType(ConfigKey key, String type) {
        return String.format("%n%n%n" +
                "### Notes:%n" +
                "### 1. The value that should be passed to %1$s should be %2$s.%n" +
                "### 2. Possible values to be provided are : %3$s %n" +
                "### 3. In order for your change(s) to take effect,%n" +
                "###    restart the oVirt engine service (using: 'service ovirt-engine restart').%n" +
                "################################################################################%n%n",
                key.getKey(), type,
                getPossibleValues(key));
    }

    protected String getPossibleValues(ConfigKey key) {
        if (key.getValidValues() == null || key.getValidValues().size() == 0) {
            return "";
        }
        return StringUtils.join(key.getValidValues(), ",");
    }

}
