package org.ovirt.engine.core.common.businessentities;

/**
 * Specifies the grant mode of the permissions to the user.
 */
public enum PermissionGrantMode {
    /**
     * Permission was granted by a User
     */
    Manual,

    /**
     * Permission was granted automatically by the system
     */
    Automatic;

    /**
     * Returns the value of a given enum, where default for null is {@link #Manual}
     *
     * @param grantMode
     *            the returned enum value
     * @return a non-null value of the enum
     */
    public static PermissionGrantMode nullSafeValueOf(PermissionGrantMode grantMode) {
        return grantMode == null ? Manual : grantMode;
    }
}
