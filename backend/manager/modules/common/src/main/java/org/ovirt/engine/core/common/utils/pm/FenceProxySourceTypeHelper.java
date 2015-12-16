package org.ovirt.engine.core.common.utils.pm;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;

/**
 * Contains helper methods for {@code FenceProxySourceType}
 */
public class FenceProxySourceTypeHelper {
    /**
     * Parses the list of fence proxy source type from comma separated string. The resulting list is ordered
     * in the same way as it's specified in the string
     *
     * @param stringValue
     *            comma separated string of fence proxy source types
     * @return list of parsed fence proxy source type
     * @throws IllegalArgumentException
     *             if invalid fence proxy source type string value was specified in {@code stringValue}
     */
    public static List<FenceProxySourceType> parseFromString(String stringValue) {
        List<FenceProxySourceType> parsedTypes = new LinkedList<>();
        if (stringValue != null && stringValue.length() > 0) {
            String[] stringParts = stringValue.split(",");
            for (String part : stringParts) {
                parsedTypes.add(FenceProxySourceType.forValue(part));
            }
        }
        return parsedTypes;
    }

    /**
     * Saves list of fence proxy source type to comma separated string. Fence proxy source types in the resulting
     * string are ordered in the same way as in the specified list
     *
     * @param fenceProxySourceTypes
     *            list of fence proxy source types
     * @return  comma separated string of fence proxy source types
     * @throws IllegalArgumentException
     *             if {@code null} value was contained is the specified list
     */
    public static String saveAsString(List<FenceProxySourceType> fenceProxySourceTypes) {
        if (fenceProxySourceTypes == null || fenceProxySourceTypes.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (FenceProxySourceType sourceType : fenceProxySourceTypes) {
            if (sourceType == null) {
                throw new IllegalArgumentException("Null value found in the specified list");
            }

            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(sourceType.getValue());
        }
        return sb.toString();
    }
}
