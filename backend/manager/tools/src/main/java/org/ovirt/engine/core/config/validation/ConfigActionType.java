package org.ovirt.engine.core.config.validation;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.config.EngineConfigMap;

/**
 * The <code>ConfigAction</code> enum holds the optional actions in the EngineConfig tool. The enum runs validations that
 * are action-specific. Each ConfigAction holds a list of strings that represent it, and a list of validators that are
 * relevant for it.
 */
public enum ConfigActionType {
    ACTION_ALL(Arrays.asList(new String[] { "-a", "--all" }), null),
    ACTION_LIST(Arrays.asList(new String[] { "-l", "--list" }), null),
    ACTION_GET(Arrays.asList(new String[] { "-g", "--get" }), new ValidatorType[] { ValidatorType.get }),
    ACTION_SET(Arrays.asList(new String[] { "-s", "--set" }), new ValidatorType[] { ValidatorType.set }),
    ACTION_HELP(Arrays.asList(new String[] { "-h", "--help" }), new ValidatorType[] { ValidatorType.help }),
    ACTION_RELOAD(Arrays.asList(new String[] { "-r", "--reload" }), null),
    ACTION_MERGE(Arrays.asList(new String[] { "-m", "--merge" }), new ValidatorType[] { ValidatorType.set }),
    ACTION_DIFF(Arrays.asList(new String[] { "-d", "--diff" }), null);


    private List<String> optionalStrings;
    private ValidatorType[] validatorTypes;

    private ConfigActionType(List<String> optionalStrings, ValidatorType[] validationTypes) {
        this.optionalStrings = optionalStrings;
        this.validatorTypes = validationTypes;
    }

    public static ConfigActionType getActionType(String arg) {
        for (ConfigActionType type : ConfigActionType.values()) {
            if (type.optionalStrings.contains(arg)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Iterates through the list of action specific validators of this ConfigAction
     * @param engineConfigMap
     *            Should hold the arguments needed in order to validate this ConfigAction.
     */
    public void validate(EngineConfigMap engineConfigMap) throws IllegalArgumentException {
        if (validatorTypes != null) {
            for (ValidatorType val : validatorTypes) {
                EngineConfigValidatorFactory.instance().getValidation(val).validate(this, engineConfigMap);
            }
        }
    }
}
