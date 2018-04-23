package org.ovirt.engine.core.config;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.config.validation.ConfigActionType;

/**
 * The <code>EngineConfigCLIParser</code> class represents a parser for the EngineConfig tool. It parses the given
 * arguments into meaningful keys and values. The parser assumes the '=' char can only be used in the format k=v, and
 * not as a char that is actually part of a key/value.
 */
public class EngineConfigCLIParser {
    private Map<String, String> argsMap = new HashMap<>();
    private EngineConfigMap engineConfigMap = new EngineConfigMap();

    public EngineConfigCLIParser() {
    }

    /**
     * Parses the given arguments, identifies the desired action, and the different keys and values.
     *
     * @param args
     *            The arguments that need to be parsed.
     * @throws IllegalArgumentException
     *             If there are no arguments, if a legal action was not identified, or if second argument has '=' char,
     *             but action is not 'set'.
     */
    public void parse(String[] args) {
        validateNonEmpty(args);
        parseAction(args);
        parseArguments(args);
    }

    private void validateNonEmpty(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Error: at least 1 argument needed for configuration utility to run.");
        }
    }

    /**
     * Parses the argument in the currentIndex of args, into a key and its value. Can also parse version, properties
     * file, config file. If the argument in the currentIndex does not have a value, assumes the value is in the
     * following argument. The argsMap helps to parse the arguments which are eventually set into the engineConfigMap.
     *
     * @return whether or not the next argument is to be skipped
     */
    private boolean parseKeyValue(String[] args, int currentIndex) {
        boolean fShouldSkip = false;
        int delimiterIndex = args[currentIndex].indexOf("=");
        String key = getStringBeforeEqualChar(args[currentIndex], delimiterIndex); // includes '-'
        String value = getStringAfterEqualChar(args[currentIndex], delimiterIndex);
        if (OptionKey.OPTION_ONLY_RELOADABLE.getOptionalStrings().contains(key)) {
            setOnlyReloadableOption(key);
        } else if (!key.isEmpty()) {
            if (!value.isEmpty()) {
                argsMap.put(key, value);
            } else {
                if (args.length > currentIndex + 1) { // To make sure there is another argument
                    argsMap.put(key, args[currentIndex + 1]);
                    fShouldSkip = true;
                }
            }
        }
        return fShouldSkip;
    }

    private void setOnlyReloadableOption(String key) {
        argsMap.put(key, Boolean.toString(true));
    }

    /**
     * Parses the second argument in case it does not start with a '-'. There are two valid scenarios for this. First,
     * when the argument has been given as the key in the 'get' action, in the format: "-g key". Second, in the 'set'
     * action. For set action, we require key=value, or parsing will fail.
     */
    private void parseSecondArgWithoutDash(String arg, boolean passFileExists) {
        int delimiterIndex = arg.indexOf("=");
        if (isSetOrMergeAction()
                && delimiterIndex == -1
                && !passFileExists) {
            throw new IllegalArgumentException("Argument for set/merge action must be in format of key=value.");
        }

        String key = getStringBeforeEqualChar(arg, delimiterIndex);
        String value = getStringAfterEqualChar(arg, delimiterIndex);
        if (!key.isEmpty()) {
            if (!value.isEmpty()) {
                parseSecondArgWithKeyValue(arg, key, value);
            } else if (isSetOrMergeAction() && getKey() == null) {
                engineConfigMap.setKey(key);
                engineConfigMap.setValue(value);
            } else if ((getConfigAction().equals(ConfigActionType.ACTION_GET) || getConfigAction().equals(ConfigActionType.ACTION_HELP))
                    && getKey() == null) {
                engineConfigMap.setKey(arg); // sets the key in 'get' action with format: "-g key"
            }
        }
    }

    /**
     * Parses second argument with a key and a value. Is only valid in the 'set' action.
     */
    private void parseSecondArgWithKeyValue(String arg, String key, String value) {
        if (isSetOrMergeAction()) {
            engineConfigMap.setKey(key);
            engineConfigMap.setValue(value);
        } else {
            throw new IllegalArgumentException("Illegal second argument: " + arg + ".");
        }
    }

    private boolean isSetOrMergeAction() {
        return getConfigAction().equals(ConfigActionType.ACTION_SET)
                || getConfigAction().equals(ConfigActionType.ACTION_MERGE);
    }

    /**
     * Parses all arguments except for the first argument which is assumed to be the action. The argsMap member helps to
     * parse the arguments which are eventually set into the engineConfigMap.
     *
     * @param args
     *            The arguments that needs to be parsed.
     */
    private void parseArguments(String[] args) {
        boolean fShouldSkip = true; // So the first arg which is the action will be skipped
        boolean passFileExists = false;
        for (String arg : args) {
            if (arg.startsWith("--admin-pass-file")) {
                passFileExists = true;
                break;
            }
        }
        for (int currentIndex = 0; currentIndex < args.length; currentIndex++) {
            if (fShouldSkip) {
                fShouldSkip = false;
                continue;
            }
            if (args[currentIndex].startsWith("-")) {
                fShouldSkip = parseKeyValue(args, currentIndex);
            } else if (currentIndex == 1) {
                parseSecondArgWithoutDash(args[currentIndex], passFileExists);
            }
        }
        fillEngineConfigMap();
    }

    /**
     * Parses the action from the given arguments.
     *
     * @throws IllegalArgumentException
     *             If the first argument is not a legal action
     */
    private void parseAction(String[] args) {
        validateArgStartsWithDash(args[0]);
        int delimiterIndex = args[0].indexOf("=");
        String action = getStringBeforeEqualChar(args[0], delimiterIndex);
        String key = getStringAfterEqualChar(args[0], delimiterIndex);
        if (!action.isEmpty()) {
            if (!key.isEmpty()) {
                handleActionWithKey(action, key);
            } else {
                handleActionWithoutKey(action);
            }
        } else {
            throw new IllegalArgumentException("Action verb must come first, and '" + args[0]
                    + "' is not an action.\nPlease tell me what to do: list? get? set? get-all?");
        }
    }

    /**
     * Returns the first part of the given arg, until the delimiterIndex, excluding. Did not use split() because of
     * problematic handling of empty parts.
     */
    private String getStringAfterEqualChar(String arg, int delimiterIndex) {
        String value;
        if (delimiterIndex > 0) {
            value = arg.substring(delimiterIndex + 1);
        } else {
            value = "";
        }
        return value;
    }

    /**
     * Returns the second part of the given arg, starting from the delimiterIndex, excluding. Did not use split()
     * because of problematic handling of empty parts.
     */
    private String getStringBeforeEqualChar(String arg, int delimiterIndex) {
        String key;
        if (delimiterIndex > 0) {
            key = arg.substring(0, delimiterIndex);
        } else {
            key = arg;
        }
        return key;
    }

    /**
     * Handles an action without a key.
     */
    private void handleActionWithoutKey(String action) {
        engineConfigMap.setConfigAction(ConfigActionType.getActionType(action));
        if (getConfigAction() == null) {
            throw new IllegalArgumentException("Action verb must come first, and '" + action
                    + "' is not an action.\nPlease tell me what to do: list? get? set? get-all?");
        }
    }

    /**
     * Handles an action with a key. The only valid action with a key in the first argument is the 'get' action, in the
     * format: "--get=key".
     */
    private void handleActionWithKey(String action, String key) {
        engineConfigMap.setConfigAction(ConfigActionType.getActionType(action));
        if (action.equals("--get") || action.equals("--help")) {
            engineConfigMap.setKey(key);
        } else {
            throw new IllegalArgumentException("Action verb must come first, and '" + action + '=' + key
                    + "' is not an action.\nPlease tell me what to do: list? get? set? get-all?");
        }
    }

    /**
     * Makes sure the first argument starts with a '-', since all actions do.
     */
    private void validateArgStartsWithDash(String arg) {
        if (!arg.startsWith("-")) {
            throw new IllegalArgumentException("First argument must be an action, and start with '-' or '--'");
        }
    }

    private String parseOptionKey(OptionKey optionKey) {
        for (String configKeyName : optionKey.getOptionalStrings()) {
            if (argsMap.containsKey(configKeyName)) {
                return argsMap.get(configKeyName);
            }
        }
        return null;
    }

    private void fillEngineConfigMap() {
        engineConfigMap.setVersion(parseOptionKey(OptionKey.OPTION_VERSION));
        engineConfigMap.setAlternateConfigFile(parseOptionKey(OptionKey.OPTION_CONFIG));
        engineConfigMap.setAlternatePropertiesFile(parseOptionKey(OptionKey.OPTION_PROPERTIES));
        engineConfigMap.setUser(parseOptionKey(OptionKey.OPTION_USER));
        engineConfigMap.setAdminPassFile(parseOptionKey(OptionKey.OPTION_ADMINPASSFILE));
        engineConfigMap.setOnlyReloadable(parseOptionKey(OptionKey.OPTION_ONLY_RELOADABLE));
        engineConfigMap.setLogFile(parseOptionKey(OptionKey.OPTION_LOG_FILE));
        engineConfigMap.setLogLevel(parseOptionKey(OptionKey.OPTION_LOG_LEVEL));
    }

    public EngineConfigMap getEngineConfigMap() {
        return engineConfigMap;
    }

    public String getUser() {
        return engineConfigMap.getUser();
    }

    public String getAdminPassFile() {
        return engineConfigMap.getAdminPassFile();
    }

    public String getVersion() {
        return engineConfigMap.getVersion();
    }

    public ConfigActionType getConfigAction() {
        return engineConfigMap.getConfigAction();
    }

    public String getKey() {
        return engineConfigMap.getKey();
    }

    public String getValue() {
        return engineConfigMap.getValue();
    }

    public String getAlternateConfigFile() {
        return engineConfigMap.getAlternateConfigFile();
    }

    public String getAlternatePropertiesFile() {
        return engineConfigMap.getAlternatePropertiesFile();
    }

    public String engineConfigMapToString() {
        return engineConfigMap.toString();
    }

    public boolean isOnlyReloadable() {
        return engineConfigMap.isOnlyReloadable();
    }
}
