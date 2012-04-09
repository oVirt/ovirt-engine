package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.MatchCollection;
import org.ovirt.engine.core.compat.Regex;

public class EnumUtils {

    private static Map<Class<?>, Map> cacheEnumValuesInCapitalLetters = new HashMap<Class<?>, Map>();

    public static String ConvertToStringWithSpaces(String value) {
        StringBuilder result = new StringBuilder();
        Regex r = new Regex("([A-Z]{1,}[a-z]*)|([0-9]*)");
        MatchCollection coll = r.Matches(value);
        for (int i = 0; i < coll.size(); i++) {
            result.append(coll.get(i).getValue());
            if (i + 1 != coll.size()) {
                result.append(" ");
            }
        }
        return result.toString().trim();
    }

    public static <E extends Enum<E>> E valueOf(Class<E> c, String name, boolean ignorecase) {
        // trim any leading or trailing spaces from the name
        name = name.trim();

        if (!ignorecase) {
            {
                return Enum.<E> valueOf(c, name);
            }
        }

        E[] universe = c.getEnumConstants();
        if (universe == null) {
            throw new IllegalArgumentException(name + " is not an enum type");
        }

        Map<String, E> map = cacheEnumValuesInCapitalLetters.get(c);

        if (map == null) {
            // populate the map with enum values and add it to cache
            map = new HashMap<String, E>(2 * universe.length);

            for (E e : universe) {
                map.put(e.name().toUpperCase(), e);
            }
            cacheEnumValuesInCapitalLetters.put(c, map);
        }

        E result = map.get(name.toUpperCase());
        if (result == null) {
            throw new IllegalArgumentException("No enum const " + c.getName() + "." + name);
        }
        return result;
    }

    /**
     * Return the value of the {@link Enum#name()} method for the given enum instance if it's not <code>null</code>,
     * otherwise return <code>null</code>.
     *
     * @param <E>
     *            The type of the enum.
     * @param enumInstance
     *            The instance to get the name from.
     * @return <code>null</code> if the instance is null, result of {@link Enum#name()} otherwise.
     */
    public static <E extends Enum<E>> String nameOrNull(E enumInstance) {
        return enumInstance == null ? null : enumInstance.name();
    }

    /**
     * Converts the given collection of enum values to a collection of strings, containing enum names (retrieved using
     * {@link Enum#name()} method) of each element of the collection.
     *
     * @param collection
     *            The collection of Enum values to be converted to a delimiter-separated string.
     * @return A String collection containing enum names of all elements of the given collection.
     */
    public static <T extends Enum<?>> List<String> enumCollectionToStringList(Collection<T> collection) {
        if(collection == null) {
            return null;
        }

        List<String> stringList = new ArrayList<String>();

        for(T element : collection) {
            stringList.add(element.name());
        }

        return stringList;
    }
}
