package org.ovirt.engine.core.utils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.DefaultValueAttribute;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.common.config.TypeConverterAttribute;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Config Utils Base Class
 */
public abstract class ConfigUtilsBase implements IConfigUtilsInterface {
    private static final Logger log = LoggerFactory.getLogger(ConfigUtilsBase.class);

    @Override
    public abstract <T> T getValue(ConfigValues configValue, String version);

    public static final class EnumValue {
        final Class<?> fieldType;
        final String defaultValue;
        final OptionBehaviourAttribute optionBehaviour;
        public EnumValue(Class<?> fieldType, String defaultValue, OptionBehaviourAttribute optionBehaviour) {
            super();
            this.fieldType = fieldType;
            this.defaultValue = defaultValue;
            this.optionBehaviour = optionBehaviour;
        }
        public Class<?> getFieldType() {
            return fieldType;
        }
        public String getDefaultValue() {
            return defaultValue;
        }
        public OptionBehaviourAttribute getOptionBehaviour() {
            return optionBehaviour;
        }
    }

    private static final String TEMP = "Temp";

    /**
     * Returns the typed value of the given option. returns default value if option.option_value is null
     */
    protected Object getValue(VdcOption option) {
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
                        String prefix = getValue(optionBehaviour.dependentOn(), ConfigCommon.defaultConfigurationVersion);
                        // combine the prefix with the 'real value'
                        if (prefix != null) {
                            String realName = String.format("%1$s%2$s", prefix, optionBehaviour.realValue());
                            result = getValue(ConfigValues.valueOf(realName), ConfigCommon.defaultConfigurationVersion);
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
                String defaultValue = null;
                OptionBehaviourAttribute optionBehaviour = null;

                // get default value
                if (fi.isAnnotationPresent(DefaultValueAttribute.class)) {
                    defaultValue = fi.getAnnotation(DefaultValueAttribute.class).value();
                }

                // get the attribute for default behaviour
                if (fi.isAnnotationPresent(OptionBehaviourAttribute.class)) {
                    optionBehaviour = fi.getAnnotation(OptionBehaviourAttribute.class);
                }
                return new EnumValue(fieldType, defaultValue, optionBehaviour);
            } else {
                // if could not get type then cannot continue
                return null;
            }
        }
    }
}
