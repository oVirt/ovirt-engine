package org.ovirt.engine.core.config.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.config.EngineConfig;
import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.helper.StringValueHelper;
import org.ovirt.engine.core.config.entity.helper.ValueHelper;
import org.slf4j.LoggerFactory;

public class ConfigKeyFactory {

    private HierarchicalConfiguration keysConfig;
    private Map<String, String> alternateKeysMap;
    private static ConfigKeyFactory instance;
    private EngineConfigCLIParser parser;

    static {
        instance = new ConfigKeyFactory();
    }

    private ConfigKeyFactory() {
    }

    public static void init(HierarchicalConfiguration keysConfig,
            Map<String, String> alternateKeysMap,
            EngineConfigCLIParser parser) {
        instance.keysConfig = keysConfig;
        instance.alternateKeysMap = alternateKeysMap;
        instance.parser = parser;
    }

    public static ConfigKeyFactory getInstance() {
        return instance;
    }

    public ConfigKey generateByPropertiesKey(String key) {
        SubnodeConfiguration configurationAt = null;
        try {
            if (Character.isLetter(key.charAt(0))) {
                configurationAt = keysConfig.configurationAt(key);
            }
        } catch (IllegalArgumentException e) {
            // Can't find a key. maybe its an alternate key.
        }
        if (configurationAt == null || configurationAt.isEmpty()) {
            key = alternateKeysMap.get(key);
            configurationAt = keysConfig.configurationAt(key);
        }

        String type = configurationAt.getString("type");
        if (StringUtils.isBlank(type)) {
            type = "String";
        }
        String[] validValues = configurationAt.getStringArray("validValues");

        // Description containing the list delimiter *will* be broken into an array, so rejoin it using that delimiter.
        // We pad the separator because the strings in the array are trimmed automatically.
        String description = StringUtils.join(configurationAt.getStringArray("description"),
                configurationAt.getListDelimiter() + " ");
        String alternateKey = keysConfig.getString("/" + key + "/" + "alternateKey");

        // If the isReloadable attribute isn't specified - assume it is false
        boolean reloadable = configurationAt.getBoolean("isReloadable", false);
        ConfigKey configKey = new ConfigKey(type, description, alternateKey, key, "", validValues,
                "", getHelperByType(type), reloadable, isDeprecated(key));
        configKey.setParser(parser);
        return configKey;
    }

    private boolean isDeprecated(String key) {
        final String pathToDeprecatedAttribute = key + "/deprecated";
        if (!keysConfig.containsKey(pathToDeprecatedAttribute)) {
            return false;
        }

        final List list = keysConfig.getList(pathToDeprecatedAttribute);
        if (list.size() != 1) {
            throw new IllegalArgumentException("Configuration error; Key \""+key+"\" has ambiguous definition.");
        }

        final Object value = list.iterator().next();
        try {
            return Boolean.parseBoolean((String) value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Configuration error;" +
                    " Key \""+key+"\" should be either 'true' or 'false'. Value '"+value+"' is not valid.");
        }
    }

    public ConfigKey generateBlankConfigKey(String keyName, String keyType) {
        return new ConfigKey(keyType, "", "", keyName, "", null, "", getHelperByType(keyType), false, false);
    }

    private ValueHelper getHelperByType(String type) {
        ValueHelper valueHelper;
        try {
            if (type == null) {
                type = "String";
            }
            Class<?> cls = Class.forName("org.ovirt.engine.core.config.entity.helper." + type + "ValueHelper");
            valueHelper = (ValueHelper) cls.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            // failed finding a helper for this type. Setting default string type
            LoggerFactory.getLogger(EngineConfig.class)
                    .debug("Unable to find {} type. Using default string type.", type);
            valueHelper = new StringValueHelper();
        }
        return valueHelper;
    }

    public ConfigKey copyOf(ConfigKey key, String value, String version) {
        return new ConfigKey(
                             key.getType(),
                             key.getDescription(),
                             key.getAlternateKeys(),
                             key.getKey(),
                             value,
                             key.getValidValues().toArray(new String[0]),
                             version,
                             key.getValueHelper(),
                             key.isReloadable(),
                             key.isDeprecated());
    }

    /**
     * Create a ConfigKey from a ResultSet object. <b>Note</b>: Some fields are not represented by the DB, such as
     * decription and type.<br>
     *
     * TODO Consider refactoring the entity to be composed out of a real value-object which will represent the db
     *      entity and a view-object which will represent the user interaction (view) layer.
     * TODO move "option_name" and other column indexes to Enum values.
     */
    public ConfigKey fromResultSet(ResultSet resultSet) throws SQLException {
        ConfigKey configKey = generateByPropertiesKey(resultSet.getString("option_name"));
        configKey.unsafeSetValue(resultSet.getString("option_value"));
        configKey.setVersion(resultSet.getString("version"));
        configKey.setDefaultValue(resultSet.getString("default_value"));
        return configKey;
    }
}
