package org.ovirt.engine.ui.frontend.server.dashboard.maps;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;

/**
 * Map Gluster Volume statuses to one of the following statuses:
 * <ul>
 * <li>UP</li>
 * <li>WARNING</li>
 * <li>DOWN</li>
 * </ul>
 */
public enum GlusterVolumeStatusMap {
    UP(GlusterStatus.UP),
    WARNING(GlusterStatus.UNKNOWN, GlusterStatus.WARNING),
    DOWN(GlusterStatus.DOWN);

    private GlusterStatus[] values;

    private GlusterVolumeStatusMap(GlusterStatus... values) {
        this.values = values.clone();
    }

    /**
     * Check if the passed in value maps onto the enum type.
     * @param value
     *            A {@code String} value that is based on the index into Gluster Volume Status
     * @return true if the index maps into this enum value, false otherwise.
     */
    public boolean isType(String value) {
        for (GlusterStatus status : values) {
            if (status.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return An array of lower case strings that represent the mapping associated with this enum value.
     */
    public String[] getStringValues() {
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].name().toLowerCase();
        }
        return result;
    }
}
