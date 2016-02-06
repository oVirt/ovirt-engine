package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.compat.Guid;

public class GuidUtils {
    private static final String SEPARATOR = ",";

    /**
     * Gets a string containing multiple <code>Guid</code> values separated by a comma and returns an ArrayList of
     * <code>Guid</code>. If the String is null/empty returns an empty array.
     *
     * @param str
     *            - String which contains list of <code>Guid</code>.
     * @return - Array of <code>Guid</code> type.
     */
    public static ArrayList<Guid> getGuidListFromString(String str) {
        return Arrays.stream(StringUtils.split(Objects.toString(str, StringUtils.EMPTY), SEPARATOR))
                .map(Guid::createGuidFromStringDefaultEmpty)
                .collect(Collectors.toCollection(ArrayList<Guid>::new));
    }
}
