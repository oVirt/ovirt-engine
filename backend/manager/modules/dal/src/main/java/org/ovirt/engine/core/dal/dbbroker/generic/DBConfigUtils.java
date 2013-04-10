package org.ovirt.engine.core.dal.dbbroker.generic;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
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
import org.ovirt.engine.core.dao.VdcOptionDAO;
import org.ovirt.engine.core.utils.crypt.EncryptionUtils;
import org.ovirt.engine.core.utils.ConfigUtilsBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;

public class DBConfigUtils extends ConfigUtilsBase {
    private static final Map<String, Map<String, Object>> _vdcOptionCache = new HashMap<String, Map<String, Object>>();

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
            } else if (fieldType.isEnum()) {
                return Enum.valueOf((Class<Enum>)fieldType, value.toUpperCase());
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
        EnumValue enumValue = ParseEnumValue(option.getoption_name());
        if (enumValue != null) {
            final OptionBehaviourAttribute optionBehaviour = enumValue.getOptionBehaviour();
            final Class<?> fieldType = enumValue.getFieldType();
            final String defaultValue = enumValue.getDefaultValue();
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
    protected void SetValue(String name, String value, String version) {
        VdcOption vdcOption = dbfacade.getVdcOptionDao().getByNameAndVersion(name, version);
        vdcOption.setoption_value(value);
        dbfacade.getVdcOptionDao().update(vdcOption);
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
                && values.containsKey(ConfigCommon.defaultConfigurationVersion)) {
            returnValue = (String) values.get(ConfigCommon.defaultConfigurationVersion);
        } else {
            VdcOption option = DbFacade.getInstance().getVdcOptionDao().getByNameAndVersion(name.name(),
                    ConfigCommon.defaultConfigurationVersion);
            if (option != null) {
                returnValue = option.getoption_value();
            }
        }

        return returnValue;
    }

    @Override
    protected Object GetValue(DataType type, String name, String defaultValue) {
        // Note that the type parameter is useless, it should be removed (and
        // maybe all the method) in a future refactoring.
        log.warnFormat("Using old GetValue for {0}.", name);
        ConfigValues value = ConfigValues.valueOf(name);
        return GetValue(value, ConfigCommon.defaultConfigurationVersion);
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
                log.debug("Adding new value to configuration cache.");
            }
            log.debugFormat("Didn't find the value of {0} in DB for version {1} - using default: {2}",
                    name, version, returnValue);
        }

        return returnValue;
    }

    /**
     * Refreshes only the reloadable configurations in the VDC option cache.
     */
    public static void refreshReloadableConfigsInVdcOptionCache() {
        List<VdcOption> list = getVdcOptionDAO().getAll();
        for (VdcOption option : list) {
            try {
                if (isReloadable(option.getoption_name())) {
                    updateOption(option);
                }
            } catch (NoSuchFieldException e) {
                log.errorFormat("Not refreshing field {0}: does not exist in class {1}.", option.getoption_name(),
                        ConfigValues.class.getSimpleName());
            }
        }
    }

    private static VdcOptionDAO getVdcOptionDAO() {
        return DbFacade.getInstance().getVdcOptionDao();
    }

    private static void updateOption(VdcOption option) {
        Map<String, Object> values = _vdcOptionCache.get(option.getoption_name());
        if (values == null) {
            values = new HashMap<String, Object>();
            _vdcOptionCache.put(option.getoption_name(), values);
        }
        values.put(option.getversion(), GetValue(option));
    }

    private static boolean isReloadable(String optionName) throws NoSuchFieldException {
        return ConfigValues.class.getField(optionName).isAnnotationPresent(Reloadable.class);
    }

    private static Log log = LogFactory.getLog(DBConfigUtils.class);
}
