package org.ovirt.engine.core.utils;

import java.lang.reflect.Field;

import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.DataType;
import org.ovirt.engine.core.common.config.DefaultValueAttribute;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.common.config.TypeConverterAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Config Utils Base Class
 */
public abstract class ConfigUtilsBase implements IConfigUtilsInterface {
    private static final Logger log = LoggerFactory.getLogger(ConfigUtilsBase.class);

    @Override
    public final void setStringValue(String name, String value) {
        setValue(name, value, ConfigCommon.defaultConfigurationVersion);
    }

    protected abstract void setValue(String name, String value, String version);

    protected abstract Object getValue(DataType type, String name, String defaultValue);

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
