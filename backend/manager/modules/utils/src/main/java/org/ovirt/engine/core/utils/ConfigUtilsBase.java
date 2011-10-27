package org.ovirt.engine.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.DataType;
import org.ovirt.engine.core.common.config.DefaultValueAttribute;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.common.config.TypeConverterAttribute;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.TimeSpan;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;

/**
 * Config Utils Base Class
 */
public abstract class ConfigUtilsBase implements IConfigUtilsInterface {

    @Override
    public final boolean getBoolValue(String name, String defaultValue) {
        return ((Boolean) GetValue(DataType.Bool, name, defaultValue)).booleanValue();
    }


    @Override
    public final int getIntValue(String name, String defaultValue) {
        return ((Integer) GetValue(DataType.Int, name, defaultValue)).intValue();
    }


    @Override
    public final Date getDateTimeValue(String name, String defaultValue) {
        Date dt = new java.util.Date(0);
        String dateString = GetValue(DataType.String, name, defaultValue).toString();
        try {
            dt = (new SimpleDateFormat("k:m:s")).parse(dateString);
        } catch (Exception e) {
            // eat it, the value is null
        }
        return dt;
    }


    @Override
    public final TimeSpan getTimeSpanValue(String name, String defaultValue) {
        return (TimeSpan) GetValue(DataType.TimeSpan, name, defaultValue);
    }


    @Override
    public final Version getVersionValue(String name, String defaultValue) {
        return (Version) GetValue(DataType.Version, name, defaultValue);
    }


    @Override
    public final String getPathValue(String name, String defaultValue) {
        return (String) GetValue(DataType.String, name, defaultValue);
    }


    @Override
    public final void SetStringValue(String name, String value) {
        SetValue(name, value, Config.DefaultConfigurationVersion);
    }


    @Override
    public final boolean ValidateParse(DataType type, String valueInApp, String defaultValue,
            RefObject<Object> returnValue) {
        boolean castSucceded = true;
        switch (type) {
        case Bool: {
            if (valueInApp != null) {
                returnValue.argvalue = Boolean.parseBoolean(valueInApp);
            } else {
                castSucceded = false;
                returnValue.argvalue = Boolean.parseBoolean(defaultValue);
            }
            break;
        }
        case Int: {
            try {
                returnValue.argvalue = new Integer(valueInApp);
            } catch (NumberFormatException e) {
                castSucceded = false;
                returnValue.argvalue = new Integer(defaultValue);
            }
            break;
        }
        case String: {
            if (valueInApp != null) {
                returnValue.argvalue = valueInApp;
            } else {
                castSucceded = false;
                returnValue.argvalue = defaultValue;
            }
            break;
        }
        case DateTime: {
            SimpleDateFormat fmt = new SimpleDateFormat("k:m:s");
            java.util.Date val;
            try {
                val = fmt.parse(valueInApp);
            } catch (Exception e) {
                castSucceded = false;
                try {
                    val = fmt.parse(defaultValue);
                } catch (Exception e2) {
                    val = null;
                }
            }
            returnValue.argvalue = val;
            break;
        }
        case TimeSpan: {
            TimeSpan val;
            try {
                val = TimeSpan.Parse(valueInApp);
            } catch (Exception e) {
                castSucceded = false;
                try {
                    val = TimeSpan.Parse(defaultValue);
                } catch (Exception e2) {
                    val = null;
                }
            }
            returnValue.argvalue = val;
            break;
        }
        case Version: {
            Version val;
            try {
                val = new Version(valueInApp);
            } catch (Exception e) {
                castSucceded = false;
                try {
                    val = new Version(defaultValue);
                } catch (Exception e2) {
                    val = null;
                }
            }
            returnValue.argvalue = val;
            break;
        }
        }
        return castSucceded;
    }

    protected abstract void SetValue(String name, String value, String version);

    protected abstract Object GetValue(DataType type, String name, String defaultValue);


    @Override
    public abstract <T> T GetValue(ConfigValues configValue, String version);

    /**
     * parse the enum value by its attributes and return the type, default value, and option behaviour (if any) return
     * false if cannot find value in enum or cannot get type
     *
     * @param name
     * @param fieldType
     * @param defaultValue
     * @param optionBehaviour
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected static boolean ParseEnumValue(String name, RefObject<java.lang.Class> fieldType,
            RefObject<String> defaultValue, RefObject<OptionBehaviourAttribute> optionBehaviour) {
        final String TEMP = "Temp";
        boolean succeeded = true;
        optionBehaviour.argvalue = null;
        defaultValue.argvalue = null;
        fieldType.argvalue = null;

        java.lang.Class t = ConfigValues.class;
        // get field from enum for its attributes
        java.lang.reflect.Field fi = null;
        try {
            fi = t.getField(name);
        } catch (Exception ex) {
            // eat this exception. it is not fatal and will be handled like
            // fi==null;
        }

        if (fi == null) {
            // Ignore temporary values inserted to vdc_options by upgrades as
            // flags.
            if (!name.startsWith(TEMP)) {
                log.warnFormat("Could not find enum value for option: {0}", name);
            }
            succeeded = false;
        }

        else {
            // get type
            if (fi.isAnnotationPresent(TypeConverterAttribute.class)) {
                fieldType.argvalue = fi.getAnnotation(TypeConverterAttribute.class).value();

                // get default value
                if (fi.isAnnotationPresent(DefaultValueAttribute.class)) {
                    defaultValue.argvalue = fi.getAnnotation(DefaultValueAttribute.class).value();
                }

                // get the attribute for default behaviour
                if (fi.isAnnotationPresent(OptionBehaviourAttribute.class)) {
                    optionBehaviour.argvalue = fi.getAnnotation(OptionBehaviourAttribute.class);
                }
            } else {
                // if could not get type then cannot continue
                succeeded = false;
            }
        }
        return succeeded;
    }

    private static LogCompat log = LogFactoryCompat.getLog(ConfigUtilsBase.class);

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getMapValue(final String name, final String defaultValue) {
        final String jsonString = (String) GetValue(DataType.String, name, defaultValue);
        if(jsonString != null) {
            return new JsonObjectDeserializer().deserialize(jsonString, HashMap.class);
        } else {
            return null;
        }
    }
}
