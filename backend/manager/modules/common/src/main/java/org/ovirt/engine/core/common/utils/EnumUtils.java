package org.ovirt.engine.core.common.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnumUtils {

    private static final ConcurrentMap<Class<?>, Map> cacheEnumValuesInCapitalLetters = new ConcurrentHashMap<>();

    public static <E extends Enum<E>> E valueOf(Class<E> c, String name, boolean ignorecase) {
        // trim any leading or trailing spaces from the name
        name = name.trim();

        if (!ignorecase) {
            return Enum.valueOf(c, name);
        }

        E[] universe = c.getEnumConstants();
        if (universe == null) {
            throw new IllegalArgumentException(name + " is not an enum type");
        }

        Map<String, E> map =
                cacheEnumValuesInCapitalLetters.computeIfAbsent(c,
                        k -> Arrays.stream(universe).collect(Collectors.toMap(
                                e -> e.name().toUpperCase(), Function.identity())));

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
}
