package org.ovirt.engine.core.config.validation;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>EngineConfigValidatorFactory</code> class is a action specific validator factory, that holds a map between
 * validator types and their classes.
 */
public class EngineConfigValidatorFactory {
    private Map<ValidatorType, EngineConfigValidator> validationMap;
    private static EngineConfigValidatorFactory instance;
    static {
        instance = new EngineConfigValidatorFactory();
    }

    private EngineConfigValidatorFactory() {
        validationMap = new HashMap<>();
        validationMap.put(ValidatorType.get, new GetValidator());
        validationMap.put(ValidatorType.set, new SetValidator());
    }

    public static EngineConfigValidatorFactory instance() {
        return instance;
    }

    public EngineConfigValidator getValidation(ValidatorType type) {
        return validationMap.get(type);
    }
}
