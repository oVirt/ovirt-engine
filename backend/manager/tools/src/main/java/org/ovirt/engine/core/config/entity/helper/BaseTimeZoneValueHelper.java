package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.ConfigKey;

public abstract class BaseTimeZoneValueHelper extends BaseValueHelper {

    @Override
    public String getValue(String value) throws Exception {
        return value;
    }

    @Override
    public String setValue(String value) throws Exception {
        return value;
    }

    @Override
    public ValidationResult validate(ConfigKey key, String value) {
        if (getTimeZoneType().getTimeZoneList().containsKey(value)) {
            return new ValidationResult(true);
        }

        return new ValidationResult(false, String.format("%s is not a valid %s. %s", value, getHelpNoteType(), getExample()));
    }

    @Override
    public void setParser(EngineConfigCLIParser parser) {
        // NOP
    }

    @Override
    public String getHelpNote(ConfigKey key) {
        return getHelpNoteByType(key, getHelpNoteType());
    }

    public abstract TimeZoneType getTimeZoneType();

    public abstract String getHelpNoteType();

    public abstract String getExample();
}
