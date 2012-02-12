package org.ovirt.engine.core.dal.dbbroker.generic;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.DataType;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;
import org.ovirt.engine.core.utils.ConfigUtilsBase;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;

public class DBConfigUtils extends ConfigUtilsBase {
    private static final Map<String, Map<String, Object>> _vdcOptionCache = new HashMap<String, Map<String, Object>>();

    /**
     * Refreshes the VDC option cache.
     */
    private static void RefreshVdcOptionCache(DbFacade db) {
        _vdcOptionCache.clear();
        List<VdcOption> list = db.getVdcOptionDAO().getAll();
        for (VdcOption option : list) {
            Map<String, Object> values = null;
            if ((values = _vdcOptionCache.get(option.getoption_name())) != null) {
                values.put(option.getversion(), GetValue(option));
            } else {
                values = new HashMap<String, Object>();
                values.put(option.getversion(), GetValue(option));
                _vdcOptionCache.put(option.getoption_name(), values);
            }
        }
    }

    private static Object parseValue(String value, String name, java.lang.Class<?> fieldType) {
        if (value == null) {
            return null;
        }

        try {
            if (fieldType == Integer.class) {
                return Integer.parseInt(value);
            } else if (fieldType == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (fieldType == Version.class) {
                return new Version(value);
            } else if (fieldType == java.util.Date.class) {
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
            } else {
                return value;
            }
        } catch (java.lang.Exception e2) {
            log.errorFormat("Could not parse option {0} value.", name);
            return null;
        }
    }

    /**
     * Returns the typed value of the given option. returns default value if option.option_value is null
     *
     * @param option
     * @return
     */
    private static Object GetValue(VdcOption option) {
        Object result = option.getoption_value();
        @SuppressWarnings("rawtypes")
        java.lang.Class fieldType = null;
        String defaultValue = null;
        OptionBehaviourAttribute optionBehaviour = null;
        @SuppressWarnings("rawtypes")
        RefObject<java.lang.Class> tempRefObject = new RefObject<java.lang.Class>(fieldType);
        RefObject<String> tempRefObject2 = new RefObject<String>(defaultValue);
        RefObject<OptionBehaviourAttribute> tempRefObject3 = new RefObject<OptionBehaviourAttribute>(optionBehaviour);
        boolean tempVar = ParseEnumValue(option.getoption_name(), tempRefObject, tempRefObject2, tempRefObject3);
        fieldType = tempRefObject.argvalue;
        defaultValue = tempRefObject2.argvalue;
        optionBehaviour = tempRefObject3.argvalue;
        if (tempVar) {
            result = parseValue(option.getoption_value(), option.getoption_name(), fieldType);

            // if null use default from @DefaultValueAttribute
            if (result == null) {
                result = parseValue(defaultValue, option.getoption_name(), fieldType);
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
                        String keyFile = getValueFromDBDefault(ConfigValues.keystoreUrl);
                        String passwd = getValueFromDBDefault(ConfigValues.keystorePass);
                        String alias = getValueFromDBDefault(ConfigValues.CertAlias);
                        result = EncryptionUtils.decrypt((String) result, keyFile, passwd, alias);
                    } catch (Exception e) {
                        log.errorFormat("Failed to decrypt value for property {0} will be used encrypted value",
                                option.getoption_name(), e.getMessage());
                    }
                    break;
                case DomainsPasswordMap:
                    String keyFile = getValueFromDBDefault(ConfigValues.keystoreUrl);
                    String passwd = getValueFromDBDefault(ConfigValues.keystorePass);
                    String alias = getValueFromDBDefault(ConfigValues.CertAlias);
                    result = new DomainsPasswordMap((String) result, keyFile, passwd, alias);
                    break;
                case ValueDependent:
                    // get the config that this value depends on
                    if ((values = _vdcOptionCache.get(optionBehaviour.dependentOn().toString())) != null) {
                        // its value is this value's prefix
                        String prefix = (String) values.get(Config.DefaultConfigurationVersion);
                        // combine the prefix with the 'real value'
                        if ((values = _vdcOptionCache
                                .get(String.format("%1$s%2$s", prefix, optionBehaviour.realValue()))) != null) {
                            // get value of the wanted config - assuming
                            // default!!
                            result = values.get(Config.DefaultConfigurationVersion);
                        }
                    }
                    break;
                case CommaSeparatedVersionArray:
                    java.util.HashSet<Version> versions = new java.util.HashSet<Version>();
                    for (String ver : result.toString().split("[,]", -1)) {
                        try {
                            versions.add(new Version(ver));
                        } catch (java.lang.Exception e) {
                            log.errorFormat("Could not parse version {0} for config value {1}", ver,
                                    option.getoption_name());
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
            RefreshVdcOptionCache(dbfacade);
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
    protected void SetValue(String name, String value, String version) {
        VdcOption vdcOption = dbfacade.getVdcOptionDAO().getByNameAndVersion(name, version);
        vdcOption.setoption_value(value);
        dbfacade.getVdcOptionDAO().update(vdcOption);
        try {
            // refresh the cache entry after update
            _vdcOptionCache.get(vdcOption.getoption_name()).put(version, GetValue(vdcOption));
        } catch (java.lang.Exception e) {
            log.errorFormat("Could not update option {0} in cache.", name);
        }
    }

    private static String getValueFromDBDefault(ConfigValues name) {
        String returnValue = null;
        Map<String, Object> values = null;
        if ((values = _vdcOptionCache.get(name.toString())) != null
                && values.containsKey(Config.DefaultConfigurationVersion)) {
            returnValue = (String) values.get(Config.DefaultConfigurationVersion);
        } else {
            VdcOption option = DbFacade.getInstance().getVdcOptionDAO().getByNameAndVersion(name.name(),
                    Config.DefaultConfigurationVersion);
            if (option != null) {
                returnValue = option.getoption_value();
            }
        }

        return returnValue;
    }

    @Override
    protected Object GetValue(DataType type, String name, String defaultValue) {
        log.warnFormat("Using old GetValue for {0}.", name);
        ConfigValues value = ConfigValues.valueOf(name);
        switch (type) {
        case Bool:
            return this.<Boolean> GetValue(value, Config.DefaultConfigurationVersion);
        case DateTime:
            return this.<java.util.Date> GetValue(value, Config.DefaultConfigurationVersion);
        case Int:
            return this.<Integer> GetValue(value, Config.DefaultConfigurationVersion);
        case Version:
            return this.<Version> GetValue(value, Config.DefaultConfigurationVersion);
        case Map:
            return this.<Map<?, ?>> GetValue(value, Config.DefaultConfigurationVersion);
        default:
            return this.<String> GetValue(value, Config.DefaultConfigurationVersion);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T GetValue(ConfigValues name, String version) {
        T returnValue;
        Map<String, Object> values = null;
        if ((values = _vdcOptionCache.get(name.toString())) != null && values.containsKey(version)) {
            returnValue = (T) values.get(version);
        } else {
            VdcOption option = new VdcOption();
            option.setoption_name(name.toString());
            option.setoption_value(null);
            // returns default value - version independent
            returnValue = (T) GetValue(option);

            // If just requested version is missing, add the default value with the requested version.
            if (values != null) {
                values.put(version, returnValue);
            } else {
                // Couldn't find this value at all, adding to cache.
                Map<String, Object> defaultValues = new HashMap<String, Object>();
                defaultValues.put(version, returnValue);
                _vdcOptionCache.put(option.getoption_name(), defaultValues);
                log.warn("Adding new value to configuration cache.");
            }
            log.warnFormat("Didn't find the value of {0} in DB for version {1} - using default: {2}",
                    name, version, returnValue);
        }

        return returnValue;
    }

    private static Log log = LogFactory.getLog(DBConfigUtils.class);
}
