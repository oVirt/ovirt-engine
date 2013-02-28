package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.ConfigKey;

public interface ValueHelper {

    String getValue(String value) throws Exception;

    String setValue(String value) throws Exception;

    ValidationResult validate(ConfigKey key, String value);

    void setParser(EngineConfigCLIParser parser);

    String getHelpNote(ConfigKey key);
}
