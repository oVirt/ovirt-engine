package org.ovirt.engine.core.dal.dbbroker.generic;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.DataType;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.common.config.Reloadable;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdcOptionDao;
import org.ovirt.engine.core.utils.ConfigUtilsBase;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConfigUtils extends ConfigUtilsBase {
    private static final Logger log = LoggerFactory.getLogger(DBConfigUtils.class);

    private static final Map<String, Map<String, Object>> _vdcOptionCache = new HashMap<>();

    /**
     * Refreshes the VDC option cache.
     */
    protected static void refreshVdcOptionCache(DbFacade db) {
        _vdcOptionCache.clear();
        List<VdcOption> list = db.getVdcOptionDao().getAll();
        for (VdcOption option : list) {
            updateOption(option);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object parseValue(String value, String name, Class<?> fieldType) {
        if (value == null) {
            return null;
        }

        try {
            if (fieldType == Integer.class) {
                return Integer.parseInt(value);
            } else if (fieldType == Long.class) {
                return Long.parseLong(value);
            } else if (fieldType == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (fieldType == Version.class) {
                return new Version(value);
            } else if (fieldType == Date.class) {
                return new SimpleDateFormat("k:m:s").parse(value);
            } else if (fieldType == Double.class) {
                return Double.parseDouble(value);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                /* This if statement is to make a difference between the old and the json-style
                 * map support:
                 * - if it is enclosed between brackets, then it is handled as json object and a map is returned
                 * - otherwise it is handled as a string, the client code handles parsing it
                 */
                if(StringUtils.startsWith(value, "{") && StringUtils.endsWith(value, "}")) {
                    return new JsonObjectDeserializer().deserialize(value, HashMap.class);
                } else {
                    return value;
                }
            } else if (fieldType.isEnum()) {
                return Enum.valueOf((Class<Enum>)fieldType, value.toUpperCase());
            } else {
                return value;
            }
        } catch (Exception e2) {
            log.error("Error parsing option '{}' value: {}", name, e2.getMessage());
            log.debug("Exception", e2);
            return null;
        }
    }

    /**
     * Returns the typed value of the given option. returns default value if option.option_value is null
     */
    private static Object getValue(VdcOption option) {
        Object result = option.getOptionValue();
        EnumValue enumValue = parseEnumValue(option.getOptionName());
        if (enumValue != null) {
            final OptionBehaviourAttribute optionBehaviour = enumValue.getOptionBehaviour();
            final Class<?> fieldType = enumValue.getFieldType();
            final String defaultValue = enumValue.getDefaultValue();
            result = parseValue(option.getOptionValue(), option.getOptionName(), fieldType);

            // if null use default from @DefaultValueAttribute
            if (result == null) {
                result = parseValue(defaultValue, option.getOptionName(), fieldType);
            }

            if (optionBehaviour != null) {
                Map<String, Object> values = null;
                switch (optionBehaviour.behaviour()) {
                // split string by comma for List<string> constructor
                case CommaSeparatedStringArray:
                    result = Arrays.asList(((String) result).split("[,]", -1));
                    break;
                case Password:
                    try {
                        result = EngineEncryptionUtils.decrypt((String) result);
                    } catch (Exception e) {
                        log.error("Failed to decrypt value for property '{}', encrypted value will be used. Error: {} ",
                                option.getOptionName(), e.getMessage());
                        log.debug("Exception", e);
                    }
                    break;
                case ValueDependent:
                    // get the config that this value depends on
                    if ((values = _vdcOptionCache.get(optionBehaviour.dependentOn().toString())) != null) {
                        // its value is this value's prefix
                        String prefix = (String) values.get(ConfigCommon.defaultConfigurationVersion);
                        // combine the prefix with the 'real value'
                        if ((values = _vdcOptionCache
                                .get(String.format("%1$s%2$s", prefix, optionBehaviour.realValue()))) != null) {
                            // get value of the wanted config - assuming
                            // default!!
                            result = values.get(ConfigCommon.defaultConfigurationVersion);
                        }
                    }
                    break;
                case CommaSeparatedVersionArray:
                    HashSet<Version> versions = new HashSet<>();
                    for (String ver : result.toString().split("[,]", -1)) {
                        try {
                            versions.add(new Version(ver));
                        } catch (Exception e) {
                            log.error("Could not parse version '{}' for config value '{}'",
                                    ver, option.getOptionName());
                        }
                    }
                    result = versions;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Initializes a new instance of the <see cref="DBConfigUtils"/> class.
     */
    public DBConfigUtils() {
        this(true);
    }

    private DbFacade dbfacade = null;

    /**
     * Constructs a DBConfigUtils instance which does not use DbFacade. Used by the unit test.
     *
     * @param initDB
     *            Whether to use DbFacade
     */
    public DBConfigUtils(boolean initDB) {
        if (initDB) {
            dbfacade = DbFacade.getInstance();
            refreshVdcOptionCache(dbfacade);
        }
    }

    /**
     * Sets a vdcoption value.
     *
     * @param name
     *            The name.
     * @param value
     *            The value.
     */
    @Override
    protected void setValue(String name, String value, String version) {
        VdcOption vdcOption = dbfacade.getVdcOptionDao().getByNameAndVersion(name, version);
        vdcOption.setOptionValue(value);
        dbfacade.getVdcOptionDao().update(vdcOption);
        try {
            // refresh the cache entry after update
            _vdcOptionCache.get(vdcOption.getOptionName()).put(version, getValue(vdcOption));
        } catch (Exception e) {
            log.error("Error updating option '{}' in cache: {}", name, e.getMessage());
            log.debug("Exception", e);
        }
    }

    @Override
    protected Object getValue(DataType type, String name, String defaultValue) {
        // Note that the type parameter is useless, it should be removed (and
        // maybe all the method) in a future refactoring.
        log.warn("Using old getValue for '{}'.", name);
        ConfigValues value = ConfigValues.valueOf(name);
        return getValue(value, ConfigCommon.defaultConfigurationVersion);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(ConfigValues name, String version) {
        T returnValue;
        Map<String, Object> values = null;
        if ((values = _vdcOptionCache.get(name.toString())) != null && values.containsKey(version)) {
            returnValue = (T) values.get(version);
        } else {
            VdcOption option = new VdcOption();
            option.setOptionName(name.toString());
            option.setOptionValue(null);
            // returns default value - version independent
            returnValue = (T) getValue(option);

            // If just requested version is missing, add the default value with the requested version.
            if (values != null) {
                values.put(version, returnValue);
            } else {
                // Couldn't find this value at all, adding to cache.
                Map<String, Object> defaultValues = new HashMap<>();
                defaultValues.put(version, returnValue);
                _vdcOptionCache.put(option.getOptionName(), defaultValues);
                log.debug("Adding new value to configuration cache.");
            }
            log.debug("Didn't find the value of '{}' in DB for version '{}' - using default: '{}'",
                    name, version, returnValue);
        }

        return returnValue;
    }

    /**
     * Refreshes only the reloadable configurations in the VDC option cache.
     */
    public static void refreshReloadableConfigsInVdcOptionCache() {
        List<VdcOption> list = getVdcOptionDao().getAll();
        for (VdcOption option : list) {
            try {
                if (isReloadable(option.getOptionName())) {
                    updateOption(option);
                }
            } catch (NoSuchFieldException e) {
                log.error("Not refreshing field '{}': does not exist in class {}.", option.getOptionName(),
                        ConfigValues.class.getSimpleName());
            }
        }
    }

    private static VdcOptionDao getVdcOptionDao() {
        return DbFacade.getInstance().getVdcOptionDao();
    }

    private static void updateOption(VdcOption option) {
        Map<String, Object> values = _vdcOptionCache.get(option.getOptionName());
        if (values == null) {
            values = new HashMap<>();
            _vdcOptionCache.put(option.getOptionName(), values);
        }
        values.put(option.getVersion(), getValue(option));
    }

    private static boolean isReloadable(String optionName) throws NoSuchFieldException {
        return ConfigValues.class.getField(optionName).isAnnotationPresent(Reloadable.class);
    }
}
