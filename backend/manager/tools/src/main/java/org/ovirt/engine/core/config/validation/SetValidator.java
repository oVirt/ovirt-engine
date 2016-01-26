package org.ovirt.engine.core.config.validation;

import org.ovirt.engine.core.config.EngineConfigMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>KeyValidator</code> class is a action specific validator for the 'set' action.
 */
public class SetValidator implements EngineConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(SetValidator.class);

    /**
     * Validates that the 'set' action has a key and value.
     * @throws IllegalArgumentException
     *             If the engineConfigMap does not contain a key and value
     */
    @Override
    public void validate(ConfigActionType actionType, EngineConfigMap engineConfigMap) throws IllegalArgumentException {
        if (engineConfigMap.getKey() == null || engineConfigMap.getValue() == null) {
            log.debug("validator for 'set' action: Missing key or value for set action.");
            throw new IllegalArgumentException("Missing key or value"
                    + " for set action, make sure arguments are in proper order.");
        }
    }
}
