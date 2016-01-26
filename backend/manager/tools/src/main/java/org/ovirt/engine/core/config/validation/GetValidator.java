package org.ovirt.engine.core.config.validation;

import org.ovirt.engine.core.config.EngineConfigMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetValidator implements EngineConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(GetValidator.class);

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

