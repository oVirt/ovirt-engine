package org.ovirt.engine.core.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.compat.CompatException;

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
        String enumName = null;
        if (!enumerationType.isEnum()) {
            throw new CompatException("Class is not an Enum: " + enumerationType.getName());
        }
        try {
            for (Object obj : enumerationType.getEnumConstants()) {
                Enum enumObject = (Enum) obj;
                if (enumObject.ordinal() == val) {
                    enumName = enumObject.name();
                    break;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return enumName;
    }

    /**
     * Returns the ordinal values of the enum
     */
    public static <T extends Enum> int[] GetIntValues(Class<T> enumerationType) {

        List<T> enums = GetValues(enumerationType);
        int size = enums.size();
        int[] returnValue = new int[size];

        for (int x = 0; x < size; x++) {
            if (!(enums.get(x) instanceof Enum))
                throw new CompatException("Class is not an Enum: " + enums.get(x));

            returnValue[x] = ((Enum) enums.get(x)).ordinal();
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
