package org.ovirt.engine.core.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumCompat {

    /**
     * Returns the Enum instances as an Arraylist
     */
    public static <T extends Enum> ArrayList<T> GetValues(Class<T> clazz) {
        ArrayList<String> returnValues = new ArrayList<String>();
        if (!clazz.isEnum()) {
            throw new CompatException("Class is not an Enum: " + clazz.getName());
        }
        return new ArrayList(Arrays.asList(clazz.getEnumConstants()));
    }

    /**
     * Returns the Enum names as an Arraylist
     */
    public static <T extends Enum> String[] GetNames(Class<T> clazz) {
        ArrayList<String> returnValues = new ArrayList<String>();
        if (!clazz.isEnum()) {
            throw new CompatException("Class is not an Enum: " + clazz.getName());
        }
        for (Enum e : clazz.getEnumConstants()) {
            returnValues.add(e.name());
        }
        return returnValues.toArray(new String[0]);
    }

    /**
     * Returns the name of an enum based on its ordinal value
     */
    public static String GetName(Class enumerationType, int val) {
        Enum enumObject;
        try {
            Method forValue = enumerationType.getMethod("forValue", int.class);
            enumObject = (Enum) forValue.invoke(null, val);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return enumObject.name();
        // return ((Enum)enumerationType)..getEnumConstants()[val].toString();
    }

    /**
     * Returns the ordinal values of the enum
     */
    public static <T extends Enum> int[] GetIntValues(Class<T> enumerationType) {
        List<T> enums = GetValues(enumerationType);
        int size = enums.size();
        int[] returnValue = new int[size];
        try {
            Method getValue = enumerationType.getMethod("getValue");
            for (int x = 0; x < size; x++) {
                // returnValue[x] = enums.get(x).ordinal();
                int enumValue = (Integer) getValue.invoke(enums.get(x));
                returnValue[x] = enumValue;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return returnValue;
    }

    /**
     * Checks whether the enum name is defined
     */
    public static <T extends Enum> boolean IsDefined(Class<T> clazz, String name) {
        return Arrays.asList(EnumCompat.GetNames(clazz)).contains(name);
    }
}
