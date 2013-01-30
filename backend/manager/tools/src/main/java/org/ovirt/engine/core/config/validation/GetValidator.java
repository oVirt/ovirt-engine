package org.ovirt.engine.core.config.validation;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.config.EngineConfigMap;

public class GetValidator implements EngineConfigValidator {

    private final static Logger log = Logger.getLogger(GetValidator.class);

    /**
     * Validates that the 'get' action has a key.
     *
     * @throws IllegalArgumentException
     *             If the engineConfigMap does not contain a key.
     */
    @Override
    public void validate(ConfigActionType actionType, EngineConfigMap engineConfigMap) throws IllegalArgumentException {
            if (engineConfigMap.getKey() == null) {
            log.debug("validator for 'get' action: Missing key for get action.");
                throw new IllegalArgumentException("Missing key for get action.");
            }
    }
}

