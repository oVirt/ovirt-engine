package org.ovirt.engine.core.config.validation;

import org.ovirt.engine.core.config.EngineConfigMap;

/**
 * The <code>EngineConfigValidator</code> interface is meant to enable action specific validation for the EngineConfig
 * tool.
 */
public interface EngineConfigValidator {
    public void validate(ConfigActionType actionType, EngineConfigMap engineConfigMap) throws IllegalArgumentException;
}
