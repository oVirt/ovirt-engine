package org.ovirt.engine.core.common.businessentities;

/**
 * Status of host kdump flow
 */
public enum KdumpFlowStatus {
    /**
     * Kdump flow started
     */
    STARTED,

    /**
     * Kdump flow is currently running
     */
    DUMPING,

    /**
     * Kdump flow finished successfully
     */
    FINISHED;

    /**
     * Returns string value (lowercase name)
     */
    public String getAsString() {
        return name().toLowerCase();
    }

    /**
     * Creates an enum instance for specified string value
     *
     * @param value
     *            string value
     */
    public static KdumpFlowStatus forString(String value) {
        KdumpFlowStatus result = null;
        if (value != null) {
            result = KdumpFlowStatus.valueOf(value.toUpperCase());
        }
        return result;
    }
}
