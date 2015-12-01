package org.ovirt.engine.core.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumCompat {

    /**
     * Returns the Enum names as an Arraylist
     */
    public static <T extends Enum> String[] GetNames(Class<T> clazz) {
        ArrayList<String> returnValues = new ArrayList<>();
        if (!clazz.isEnum()) {
            throw new RuntimeException("Class is not an Enum: " + clazz.getName());
        }
        for (Enum e : clazz.getEnumConstants()) {
            returnValues.add(e.name());
        }
        return returnValues.toArray(new String[0]);
    }
}
