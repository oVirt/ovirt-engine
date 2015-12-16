package org.ovirt.engine.core.common.businessentities.pm;

/**
 * Action available in all fence agents
 */
public enum FenceActionType {
    START("on"),
    STOP("off"),
    STATUS("status");

    /**
     * String representation of action
     */
    private final String value;

    FenceActionType(String value) {
        this.value = value;
    }

    /**
     * Returns string representation of fence action
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Tries to parse fence action from string
     *
     * @param value
     *            string representation of fence action
     * @return parsed fence action
     * @throws IllegalArgumentException
     *             if invalid value was specified
     */
    public static FenceActionType forValue(String value) {
        if (value != null && value.length() > 0) {
            String lowerCase = value.toLowerCase();
            if ("on".equals(lowerCase)) {
                return START;
            } else if ("off".equals(lowerCase)) {
                return STOP;
            } else if ("status".equals(lowerCase)) {
                return STATUS;
            }
        }
        // TODO: Change to String.format() when this won't be needed in GWT
        throw new IllegalArgumentException("Invalid value '" + value + "'");
    }
}
