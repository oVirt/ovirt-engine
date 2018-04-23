package org.ovirt.engine.core.dal.dbbroker.generic;

import static org.ovirt.engine.core.common.config.OptionBehaviour.ValueDependent;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.common.config.Reloadable;
import org.ovirt.engine.core.common.config.TypeConverterAttribute;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdcOptionDao;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DBConfigUtils implements IConfigUtilsInterface {
    private static final Logger log = LoggerFactory.getLogger(DBConfigUtils.class);

    private static final String TEMP = "Temp";
    private final Map<String, Map<String, Object>> _vdcOptionCache = new HashMap<>();

    @Inject
    private VdcOptionDao vdcOptionDao;

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
     * parse the enum value by its attributes and return the type, default value, and option behaviour (if any) return
     * false if cannot find value in enum or cannot get type
     */
    protected static EnumValue parseEnumValue(String name) {

        // get field from enum for its attributes
        Field fi = null;
        try {
            fi = ConfigValues.class.getField(name);
        } catch (Exception ex) {
            // eat this exception. it is not fatal and will be handled like
            // fi==null;
        }

        if (fi == null) {
            // Ignore temporary values inserted to vdc_options by upgrades as
            // flags.
            if (!name.startsWith(TEMP)) {
                log.warn("Could not find enum value for option: '{}'", name);
            }
            return null;
        } else {
            // get type
            if (fi.isAnnotationPresent(TypeConverterAttribute.class)) {
                final Class<?> fieldType = fi.getAnnotation(TypeConverterAttribute.class).value();
                OptionBehaviourAttribute optionBehaviour = null;

                // get the attribute for default behaviour
                if (fi.isAnnotationPresent(OptionBehaviourAttribute.class)) {
                    optionBehaviour = fi.getAnnotation(OptionBehaviourAttribute.class);
                }
                return new EnumValue(fieldType, optionBehaviour);
            } else {
                // if could not get type then cannot continue
                return null;
            }
        }
    }

    /**
     * Refreshes the VDC option cache.
     */
    @PostConstruct
    public void refresh() {
        _vdcOptionCache.clear();
        List<VdcOption> list = moveDependentToEnd(vdcOptionDao.getAll());
        for (VdcOption option : list) {
            try {
                if (!_vdcOptionCache.containsKey(option.getOptionName()) ||
                        !_vdcOptionCache.get(option.getOptionName()).containsKey(option.getVersion()) ||
                        isReloadable(option.getOptionName())) {
                    updateOption(option);
                }
            } catch (NoSuchFieldException e) {
                log.error("Not refreshing field '{}': does not exist in class {}.", option.getOptionName(),
                        ConfigValues.class.getSimpleName());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getValuesForAllVersions(ConfigValues configValue) {
        return (Map<String, T>) _vdcOptionCache.get(configValue.toString());
    }

    @Override
    public <T> T getValue(ConfigValues name, String version) {
        Map<String, T> values = getValuesForAllVersions(name);
        if (valueExists(name, version)) {
            return values.get(version);
        }
        throw new IllegalArgumentException(name.toString() + " has no value for version: " + version);
    }

    @Override
    public boolean valueExists(ConfigValues configValue, String version) {
        Map<String, Object> values = getValuesForAllVersions(configValue);
        return values != null && values.containsKey(version);
    }

    private void updateOption(VdcOption option) {
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

    private List<VdcOption> moveDependentToEnd(List<VdcOption> list) {
        Predicate<VdcOption> isDependent =
            o -> {
                EnumValue parsed = parseEnumValue(o.getOptionName());
                if (parsed != null) {
                    OptionBehaviourAttribute behaviour = parsed.getOptionBehaviour();
                    return behaviour != null && behaviour.behaviour() == ValueDependent;
                }
                return false;
            };
        List<VdcOption> optionsList = list.stream().filter(isDependent.negate()).collect(Collectors.toList());
        optionsList.addAll(list.stream().filter(isDependent).collect(Collectors.toList()));
        return optionsList;
    }

    /**
     * Returns the typed value of the given option. returns default value if option.option_value is null
     */
    protected Object getValue(VdcOption option) {
        Object result = option.getOptionValue();
        EnumValue enumValue = parseEnumValue(option.getOptionName());
        if (enumValue != null) {
            final OptionBehaviourAttribute optionBehaviour = enumValue.getOptionBehaviour();
            final Class<?> fieldType = enumValue.getFieldType();
            result = parseValue(option.getOptionValue(), option.getOptionName(), fieldType);

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
                        String prefix = getValue(optionBehaviour.dependentOn(), ConfigCommon.defaultConfigurationVersion);
                        // combine the prefix with the 'real value'
                        if (prefix != null) {
                            String realName = String.format("%1$s%2$s", prefix, optionBehaviour.realValue());
                            result = getValue(ConfigValues.valueOf(realName), ConfigCommon.defaultConfigurationVersion);
                        }
                        break;
                    case CommaSeparatedVersionArray:
                        Set<Version> versions = new HashSet<>();
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

    public static final class EnumValue {
        final Class<?> fieldType;
        final OptionBehaviourAttribute optionBehaviour;
        public EnumValue(Class<?> fieldType, OptionBehaviourAttribute optionBehaviour) {
            super();
            this.fieldType = fieldType;
            this.optionBehaviour = optionBehaviour;
        }
        public Class<?> getFieldType() {
            return fieldType;
        }
        public OptionBehaviourAttribute getOptionBehaviour() {
            return optionBehaviour;
        }
    }
}
