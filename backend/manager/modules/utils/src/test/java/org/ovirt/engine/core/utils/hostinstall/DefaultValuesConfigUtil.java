package org.ovirt.engine.core.utils.hostinstall;

import java.text.SimpleDateFormat;

import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.DataType;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.ConfigUtilsBase;

/**
 * The DefaultValuesConfigUtil class represents a mock class, using for testing. The values fetched from this class are
 * default values only , which configured hard coded in the annotation DefaultValueAttribute.
 *
 * @see ConfigValues
 *
 */
public class DefaultValuesConfigUtil extends ConfigUtilsBase {

    /**
     * Parse the enum value of the option and get the value of it.
     *
     * @param option
     *            - the option name we want to get the default value for.
     * @return - default value of given option
     */
    private static Object GetValue(VdcOption option) {
        Object resultClassValue = null;
        RefObject<java.lang.Class> fieldType = new RefObject<java.lang.Class>();
        RefObject<String> defaultValue = new RefObject<String>();
        RefObject<OptionBehaviourAttribute> optionBehaviour = new RefObject<OptionBehaviourAttribute>();

        boolean isSuccedded = ParseEnumValue(option.getoption_name(),
                fieldType, defaultValue, optionBehaviour);

        if (!isSuccedded) {
            log.errorFormat("Fetching default value failed for option {0}.", fieldType);
        } else {
            resultClassValue = parseValue(defaultValue.argvalue, option.getoption_name(),
                    fieldType.argvalue);
        }

        return resultClassValue;
    }

    /**
     * Returns the value of the String value as a type parameter (depended on fieldType)
     *
     * @param value
     *            - value of parameter.
     * @param name
     *            - name of option
     * @param fieldType
     *            - class type of object.
     * @return - the value at the appropriate type class.
     */
    private static Object parseValue(String value, String name,
            java.lang.Class fieldType) {
        Object retTypeValue = null;
        if (value != null) {
            try {
                if (fieldType == Integer.class) {
                    retTypeValue = Integer.parseInt(value);

                } else if (fieldType == Boolean.class) {
                    retTypeValue = Boolean.parseBoolean(value);
                } else if (fieldType == Version.class) {
                    retTypeValue = new Version(value);
                } else if (fieldType == java.util.Date.class) {
                    retTypeValue = new SimpleDateFormat("k:m:s").parse(value);
                } else if (fieldType == Double.class) {
                    retTypeValue = Double.parseDouble(value);
                } else {
                    retTypeValue = value;
                }
            } catch (java.lang.Exception e2) {
                log.errorFormat("Could not parse option {0} value.", name);
            }
        }
        return retTypeValue;
    }

    @Override
    /**
     * For now this method is not in use for testing.
     * Should be implemented in the future.
     */
    protected void SetValue(String name, String value, String version) {
        throw new UnsupportedOperationException("DefaultValuesConfigUtil::SetValue method is unimplemented yet.");
    }

    @Override
    /**
     * For now this method is not in use for testing.
     * Should be implemented in the future.
     */
    protected Object GetValue(DataType type, String name, String defaultValue) {
        throw new UnsupportedOperationException("DefaultValuesConfigUtil::GetValue method is unimplemented yet.");
    }

    @Override
    /**
     * An override method , we don't use the version object ,
     * it is only for the test scenario.
     */
    public <T> T GetValue(ConfigValues name, String version) {
        T returnValue;
        VdcOption option = new VdcOption();
        option.setoption_name(name.toString());
        option.setoption_value(null);

        // returns default value - version independent
        returnValue = (T) GetValue(option);

        return returnValue;
    }

    // Set log for the class.
    private static Log log = LogFactory.getLog(DefaultValuesConfigUtil.class);
}
