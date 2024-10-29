package org.ovirt.engine.core.config.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.config.EngineConfig;
import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.helper.StringValueHelper;
import org.ovirt.engine.core.config.entity.helper.ValueHelper;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ConfigKeyFactory {
    private static ConfigKeyFactory instance;
    private EngineConfigCLIParser parser;
    private Map<String, JsonNode> props;

    static {
        instance = new ConfigKeyFactory();
    }

    private ConfigKeyFactory() {
    }

    public static void init(Map<String, JsonNode> props,
                            EngineConfigCLIParser parser) {
        instance.props = props;
        instance.parser = parser;
    }

    public static ConfigKeyFactory getInstance() {
        return instance;
    }

    public ConfigKey generateByPropertiesKey(String key) {
        JsonNode node = props.get(key);

        String type = node.get("type") == null ? "" : node.get("type").asText();
        if (StringUtils.isBlank(type)) {
            type = "String";
        }

        String[] validValues = null;
        List<String> parsedValue = node.findValuesAsText("validValues");
        if (!parsedValue.isEmpty()) {
            // not all existing options have validValues defined
            validValues = parsedValue.get(0).split(",");
        }

        // Description containing the list delimiter *will* be broken into an array, so rejoin it using that delimiter.
        // We pad the separator because the strings in the array are trimmed automatically.
        String description = node.get("description").asText();
        String alternateKey = node.get("alternateKey") == null ? "" : node.get("alternateKey").asText();

        // If the isReloadable attribute isn't specified - assume it is false
        boolean reloadable = (node.get("isReloadable") == null ) ? false : node.get("isReloadable").booleanValue();
        ConfigKey configKey = new ConfigKey(type, description, alternateKey, key, "", validValues,
                "", getHelperByType(type), reloadable, isDeprecated(key));
        configKey.setParser(parser);
        return configKey;
    }

    private boolean isDeprecated(String key) {
        JsonNode node = props.get(key).get("deprecated");
        if (node == null) {
            return false;
        }

        try {
            return Boolean.parseBoolean(node.asText());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Configuration error;" +
                    " Key \"" + key + "\" should be either 'true' or 'false'. Value '" + node + "' is not valid.");
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
