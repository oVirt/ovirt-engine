package org.ovirt.engine.core.common.businessentities.pm;

/**
 * Type of source which fence proxies to execute particular fence action are selected from
 */
public enum FenceProxySourceType {
    /**
     * Fence proxy is selected from the same cluster as fenced host
     */
    CLUSTER("cluster"),

    /**
     * Fence proxy is selected from the same data center as fenced host
     */
    DC("dc"),

    /**
     * Fence proxy is selected from a different data center than fenced host
     */
    OTHER_DC("other_dc");

    /**
     * String representation of proxy source type
     */
    private String value;

    FenceProxySourceType(String value) {
        this.value = value;
    }
    /**
     * Returns string representation of fence action
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Tries to parse fence proxy source type from string
     *
     * @param value
     *            string representation of fence proxy source type
     * @return parsed fence proxy source type
     * @throws IllegalArgumentException
     *             if invalid value was specified
     */
    public static FenceProxySourceType forValue(String value) {
        if (value != null && value.length() > 0) {
            String lowerCase = value.toLowerCase();
            if ("cluster".equals(lowerCase)) {
                return CLUSTER;
            } else if ("dc".equals(lowerCase)) {
                return DC;
            } else if ("other_dc".equals(lowerCase)) {
                return OTHER_DC;
            }
        }
        // TODO: Change to String.format() when this won't be needed in GWT
        throw new IllegalArgumentException("Invalid value '" + value + "'");
    }
}
