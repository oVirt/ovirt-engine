package org.ovirt.engine.core.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.config.db.ConfigDao;
import org.ovirt.engine.core.config.db.ConfigDaoImpl;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.config.entity.ConfigKeyFactory;
import org.ovirt.engine.core.config.validation.ConfigActionType;
import org.ovirt.engine.core.tools.ToolConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>EngineConfigLogic</code> class is responsible for the logic of the EngineConfig tool.
 */
public class EngineConfigLogic {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(EngineConfigLogic.class);

    // The console:
    private static final ToolConsole console = ToolConsole.getInstance();

    private static final String ALTERNATE_KEY = "alternateKey";
    private static final String MERGABLE_TOKEN = "mergable";
    private static final String DELIMITER_TOKEN = "delimiter";
    private static final String MERGE_NOT_SUPPORTED_MSG = "%s does not support merge of values.";
    private static final String MERGE_SAME_VALUE_MSG = "Merge operation cancelled as value is unchanged.";
    private static final String MERGE_PERSIST_ERR_MSG = "setValue: error merging %s value. No such entry%s.";
    private static final String KEY_NOT_FOUND_ERR_MSG = "Cannot display help for key %1$s. The key does not exist at the configuration file of engine-config.";

    private Configuration appConfig;
    private HierarchicalConfiguration keysConfig;
    private Map<String, String> alternateKeysMap;
    private ConfigKeyFactory configKeyFactory;
    private ConfigDao configDao;
    private EngineConfigCLIParser parser;

    public EngineConfigLogic(EngineConfigCLIParser parser) throws Exception {
        this.parser = parser;
        init();
    }

    /**
     * Initiates the members of the class.
     */
    private void init() throws Exception {
        log.debug("init: beginning initiation of EngineConfigLogic");
        appConfig = new AppConfig(parser.getAlternateConfigFile()).getFile();
        keysConfig = new KeysConfig<>(parser.getAlternatePropertiesFile()).getFile();
        populateAlternateKeyMap(keysConfig);
        ConfigKeyFactory.init(keysConfig, alternateKeysMap, parser);
        configKeyFactory = ConfigKeyFactory.getInstance();
        try {
            this.configDao = new ConfigDaoImpl(appConfig);
        } catch (SQLException se) {
            log.debug("init: caught connection error. Error details: ", se);
            throw new ConnectException("Connection to the Database failed. Please check that the hostname and port number are correct and that the Database service is up and running.");
        }
    }

    private void populateAlternateKeyMap(HierarchicalConfiguration config) {
        List<SubnodeConfiguration> configurationsAt = config.configurationsAt("/*/" + ALTERNATE_KEY);
        alternateKeysMap = new HashMap<>(configurationsAt.size());
        for (SubnodeConfiguration node : configurationsAt) {
            String rootKey = node.getRootNode()
                    .getParentNode().getName();
            String[] alternateKeys = config.getStringArray("/" + rootKey + "/" + ALTERNATE_KEY);
            for (String token : alternateKeys) {
                alternateKeysMap.put(token, rootKey);
            }
        }
    }

    /**
     * Executes desired action. Assumes the parser is now holding valid arguments.
     */
    public void execute() throws Exception {
        ConfigActionType actionType = parser.getConfigAction();
        log.debug("execute: beginning execution of action {}.", actionType);

        switch (actionType) {
        case ACTION_ALL:
            printAllValues();
            break;
        case ACTION_LIST:
            listKeys();
            break;
        case ACTION_GET:
            printKey();
            break;
        case ACTION_SET:
            persistValue();
            break;
        case ACTION_MERGE:
            mergeValue();
            break;
        case ACTION_HELP:
            printHelpForKey();
            break;
        case ACTION_RELOAD:
            reloadConfigurations();
            break;
        case ACTION_DIFF:
            checkDiff();
            break;
        default: // Should have already been discovered before execute
            log.debug("execute: unable to recognize action: {}.", actionType);
            throw new UnsupportedOperationException("Please tell me what to do: list? get? set? get-all? reload?");
        }
    }

    /**
     * Is the actual execution of the 'list' action ('-l', '--list')
     */
    private void listKeys() {
        if (parser.isOnlyReloadable()) {
            printReloadableKeys();
        } else {
            printAvailableKeys();
        }
    }

    private void reloadConfigurations() throws IOException {
        String user = parser.getUser();
        String adminPassFile = parser.getAdminPassFile();
        String pass = null;
        // Both user and password file were not given
        if (user == null) {
            user = startUserDialog();
            pass = startPasswordDialog(user);
            // only user was given
        } else if (adminPassFile == null) {
            pass = startPasswordDialog(user);
            // Both user and password file were given
        } else {
            pass = getPassFromFile(adminPassFile);
        }
        loginAndReload(user, pass);
    }

    public static String getPassFromFile(String passFile) throws IOException {
        File f = new File(passFile);
        if (!f.exists()) {
            return StringUtils.EMPTY;
        }
        String pass;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            pass = br.readLine();
        }
        if (pass == null) {
            return StringUtils.EMPTY;
        }
        return pass;
    }

    private void loginAndReload(String user, String pass) {
        // For now the reload action is undocumented in the help,
        // will be implemented in the next phase
        throw new UnsupportedOperationException();
    }

    /**
     * Is called when user has not been given
     *
     * @return The user
     */
    private String startUserDialog() throws IOException {
        log.debug("starting user dialog.");
        String user = null;
        while (StringUtils.isBlank(user)) {
            console.writeLine("Please enter user: ");
            user = console.readLine();
        }
        return user;
    }

    /**
     * Is called when user has been given, and all is needed from the user is password
     *
     * @return The user's password
     */
    public static String startPasswordDialog(String user) throws IOException {
        return startPasswordDialog(user, "Please enter a password");
    }

    public static String startPasswordDialog(String user, String msg) throws IOException {
        log.debug("starting password dialog.");
        String prompt = null;
        if (user != null) {
            prompt = msg + " for " + user + ": ";
        } else {
            prompt = msg + ": ";
        }
        return console.readPassword(prompt);
    }

    /**
     * Prints the values of the given key from the DB.
     */
    private void printAllValuesForKey(String key) throws Exception {
        List<ConfigKey> keysForName = getConfigDao().getKeysForName(key);
        if (keysForName.size() == 0) {
            log.debug("Failed to fetch value for key \"{}\", no such entry with default version.", key);
            throw new RuntimeException("Error fetching " + key + " value: no such entry with default version.");
        }

        for (ConfigKey configKey : keysForName) {
            console.write(key);
            console.write(": ");
            if (!configKey.isPasswordKey()) {
                console.write(configKey.getDisplayValue());
            } else {
                char[] value = configKey.getDisplayValue().toCharArray();
                console.writePassword(value);
                Arrays.fill(value, '\0');
            }
            console.write(" ");
            console.write("version: ");
            console.write(configKey.getVersion());
            console.writeLine();
        }
    }

    /**
     * Prints all configuration values. Is the actual execution of the 'get-all' action ('-a', '--all')
     */
    private void printAllValues() {
        List<ConfigurationNode> configNodes = keysConfig.getRootNode().getChildren();
        for (ConfigurationNode node : configNodes) {
            ConfigKey key = configKeyFactory.generateByPropertiesKey(node.getName());
            // TODO - move to one statement for all - time permitting;
            try {
                printAllValuesForKey(key.getKey());
            } catch (Exception exception) {
                log.error("Error while retrieving value for key \"{}\".", key.getKey(), exception);
            }
        }
    }

    /**
     * Prints all available configuration keys.
     */
    public void printAvailableKeys() {
        iterateAllKeys(configKeyFactory, keysConfig, key -> {
            printKeyInFormat(key);
            return true;
        });
    }

    public static boolean iterateAllKeys(ConfigKeyFactory factory,
            HierarchicalConfiguration config,
            ConfigKeyHandler handler) {
        List<ConfigurationNode> configNodes = config.getRootNode().getChildren();
        for (ConfigurationNode node : configNodes) {
            ConfigKey key = factory.generateByPropertiesKey(node.getName());
            if (!handler.handle(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prints all reloadable configuration keys. Is the actual execution of the 'list' action ('-l', '--list') with the
     * --only-reloadable flag
     */
    public void printReloadableKeys() {
        List<ConfigurationNode> configNodes = keysConfig.getRootNode().getChildren();
        for (ConfigurationNode node : configNodes) {
            ConfigKey key = configKeyFactory.generateByPropertiesKey(node.getName());
            if (key.isReloadable()) {
                printKeyInFormat(key);
            }
        }
    }

    private void printKeyInFormat(ConfigKey key) {
        console.writeFormat(
                "%s: %s (Value Type: %s)\n",
                key.getKey(),
                key.getDescription(),
                key.getType()
        );
    }

    /**
     * If a version has been given, prints the specific value for the key and version, otherwise prints all the values
     * for the key. Is the actual execution of the 'get' action ('-g', '--get')
     */
    private void printKey() throws Exception {
        String key = parser.getKey();
        String version = parser.getVersion();
        if (StringUtils.isBlank(version)) {
            ConfigKey configKey = getConfigKey(key);
            if (configKey == null) {
                throw new RuntimeException("Error fetching " + key
                        + " value: no such entry. Please verify key name and property file support.");
            }
            testIfConfigKeyCanBeFetchedOrPrinted(configKey);
            printAllValuesForKey(configKey.getKey());
        } else {
            printKeyWithSpecifiedVersion(key, version);
        }
    }

    /**
     * Fetches the given key with the given version from the DB and prints it.
     */
    private void printKeyWithSpecifiedVersion(String key, String version) throws Exception {
        ConfigKey configKey = fetchConfigKey(key, version);
        if (configKey == null || configKey.getKey() == null) {
            log.debug("getValue: error fetching {} value: no such entry with version '{}'.", key, version);
            throw new RuntimeException("Error fetching " + key + " value: no such entry with version '" + version
                    + "'.");
        }
        if (configKey.isPasswordKey()) {
            console.writePassword(configKey.getDisplayValue().toCharArray());
        } else {
            console.write(configKey.getDisplayValue());
        }
        console.writeLine();
    }

    /**
     * Sets the value of the given key for the given version. Is the actual execution of the 'set' action ('-s',
     * '--set')
     */
    private void persistValue() throws Exception {
        String key = parser.getKey();
        String value = parser.getValue();
        String version = parser.getVersion();
        if (version == null) {
            version = startVersionDialog(key);
        }
        boolean sucessUpdate = persist(key, value, version);
        if (!sucessUpdate) {
            log.debug("setValue: error setting {}'s value. No such entry{}{}.",
                    key,
                    version == null ? "" : " with version ",
                    version);
            throw new IllegalArgumentException("Error setting " + key + "'s value. No such entry"
                    + (version == null ? "" : " with version " + version) + ".");
        }
    }

    /**
     * Concatenates the value of the given key for the given version. Is the actual execution of the
     * 'merge' action ('-m', '--merge')
     */
    private void mergeValue() throws Exception {
        String key = parser.getKey();
        String value = parser.getValue();
        if(!keysConfig.getBoolean(key + "/" + MERGABLE_TOKEN, false)) {
            console.writeFormat(MERGE_NOT_SUPPORTED_MSG, key);
            console.writeLine();
            return;
        }
        String version = parser.getVersion();
        if (version == null) {
            version = startVersionDialog(key);
        }
        ConfigKey configKey = fetchConfigKey(key, version);
        if (configKey != null && configKey.getKey() != null && configKey.getDisplayValue().trim().length() > 0) {
            String valueInDb = configKey.getDisplayValue().trim();
            String delimiter = keysConfig.getString(key + "/" + DELIMITER_TOKEN, ";");
            value = mergedValues(value, valueInDb, delimiter);
            if(valueInDb.equals(value)) {
                console.writeFormat(MERGE_SAME_VALUE_MSG);
                console.writeLine();
                return;
            }
        }
        if (!persist(key, value, version)) {
            String msg = MessageFormat.format(MERGE_PERSIST_ERR_MSG, key, version == null ? "" : " with version " + version);
            log.debug(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private String mergedValues(String valueToAppend, String currentValue, String delimiter) {
        String retValue = currentValue;
        for (String val : valueToAppend.split(delimiter)) {
            retValue = mergedValue(val, retValue, delimiter);
        }
        return retValue;
    }

    private String mergedValue(String valueToAppend, String currentValue, String delimiter) {
        StringBuilder retValue = new StringBuilder(currentValue);
        if (!Arrays.asList(currentValue.split(delimiter)).contains(valueToAppend)) {
            if (!currentValue.endsWith(delimiter)) {
                retValue.append(delimiter);
            }
            retValue.append(valueToAppend);
        }
        return retValue.toString();
    }

    private void printHelpForKey() throws Exception {
        final String keyName = parser.getKey();
        boolean foundKey = iterateAllKeys(this.configKeyFactory, keysConfig, key -> {
            if (key.getKey().equals(keyName)) {
                console.writeLine(key.getValueHelper().getHelpNote(key));
                return false;
            }
            return true;
        });

        if (!foundKey) {
            console.writeFormat(KEY_NOT_FOUND_ERR_MSG, keyName);
        }
    }

    /**
     * Is called when it is unclear which version is desired. If only one version exists for the given key, assumes that
     * is the desired version, if more than one exist, prompts the user to choose one.
     *
     * @param key
     *            The version needs to be found for this key
     * @return A version for the given key
     */
    private String startVersionDialog(String key) throws IOException, SQLException {
        log.debug("starting version dialog.");
        String version = null;
        List<ConfigKey> keys = configDao.getKeysForName(key);
        if (keys.size() == 1) {
            version = keys.get(0).getVersion();
        } else if (keys.size() > 1) {
            while (true) {
                console.writeLine("Please select a version:");
                for (int i = 0; i < keys.size(); i++) {
                    console.writeFormat("%d. %s\n", i + 1, keys.get(i).getVersion());
                }
                int index = 0;
                try {
                    index = Integer.parseInt(console.readLine());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (index >= 1 && index <= keys.size()) {
                    version = keys.get(index - 1).getVersion();
                    break;
                }
            }
        }
        return version;
    }

    /**
     * Sets the given key with the given version to the given value. Is protected for test purposes.
     */
    protected boolean persist(String key, String value, String version) throws IllegalAccessException {
        ConfigKey configKey = configKeyFactory.generateByPropertiesKey(key);
        configKey.setVersion(version);
        String message = null;
        boolean res = true;

        if (configKey.isDeprecated()) {
            throw new IllegalAccessError("Configuration key " + key + " is deprecated, thus it cannot be set.");
        }

        try {
            configKey.safeSetValue(value);
            res = getConfigDao().updateKey(configKey) == 1;
        } catch (InvalidParameterException ipe) {
            message = ipe.getMessage();
            if (message == null) {
                message = "'" + value + "' is not a valid value for type " + configKey.getType() + ". " +
                        (configKey.getValidValues().isEmpty() ? "" : "Valid values are " + configKey.getValidValues());
            }
        } catch (Exception e) {
            message = "Error setting " + key + "'s value. " + e.getMessage();
            log.debug("Error details: ", e);
        }
        if (message != null) {
            log.debug(message);
            throw new IllegalAccessException(message);
        }

        return res;
    }

    public boolean persist(String key, String value) throws Exception {
        return persist(key, value, "");
    }

    private ConfigKey getConfigKey(String key) {
        ConfigKey ckReturn = null;
        ckReturn = configKeyFactory.generateByPropertiesKey(key);
        if (ckReturn == null || ckReturn.getKey() == null) {
            ckReturn = null;
            log.debug("getConfigKey: Unable to fetch the value of {}.", key);
        }

        return ckReturn;
    }

    public ConfigKey fetchConfigKey(String key, String version) {
        ConfigKey configKey = getConfigKey(key);
        if (configKey == null || configKey.getKey() == null) {
            log.debug("Unable to fetch the value of {} in version {}", key, version);
            return null;
        }
        testIfConfigKeyCanBeFetchedOrPrinted(configKey);
        configKey.setVersion(version);
        log.debug("Fetching key={} ver={}", configKey.getKey(), version);
        try {
            return getConfigDao().getKey(configKey);
        } catch (SQLException e) {
            return null;
        }
    }

    private void testIfConfigKeyCanBeFetchedOrPrinted(ConfigKey configKey) {
        if (configKey.isDeprecated()) {
            throw new IllegalAccessError("Configuration key " + configKey.getKey() + " is deprecated, thus cannot get its value.");
        }
    }

    private void checkDiff() {
        try {
            getConfigDao().getConfigDiff()
                    .stream()
                    .filter(configKey -> configKey.getKey() != null)
                    .forEach(configDiff -> console.writeFormat("Name: %s\n"
                                    + "Version: %s\n"
                                    + "Current: %s\n"
                                    + "Default: %s\n\n",
                            configDiff.getKey(),
                            configDiff.getVersion(),
                            configDiff.getValue(),
                            configDiff.getDefaultValue()));
        } catch (Exception e) {
            log.error("Error details: ", e);
        }
    }

    public ConfigDao getConfigDao() {
        return configDao;
    }
}
