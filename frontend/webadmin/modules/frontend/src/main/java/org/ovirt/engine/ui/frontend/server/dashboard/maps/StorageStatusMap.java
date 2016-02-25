package org.ovirt.engine.ui.frontend.server.dashboard.maps;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;

/**
 * Map Storage Domain database statuses to one of the following statuses:
 * <ul>
 *   <li>UP</li>
 *   <li>WARNING</li>
 *   <li>DOWN</li>
 * </ul>
 */
public enum StorageStatusMap {
    UP(StorageDomainStatus.Active),
    WARNING(StorageDomainStatus.Uninitialized, StorageDomainStatus.Unattached, StorageDomainStatus.Inactive,
            StorageDomainStatus.Maintenance, StorageDomainStatus.PreparingForMaintenance,
            StorageDomainStatus.Detaching, StorageDomainStatus.Activating),
    DOWN(StorageDomainStatus.Unknown, StorageDomainStatus.Locked);

    private StorageDomainStatus[] values;

    private StorageStatusMap(StorageDomainStatus ...values) {
        this.values = values.clone();
    }

    /**
     * Check if the passed in value maps onto the enum type.
     * @param value An {@code int} value that is based on the index into {@code StoragePoolStatus}
     * @return true if the index maps into this enum value, false otherwise.
     * @see StorageDomainStatus
     */
    public boolean isType(int value) {
        for (StorageDomainStatus status :values) {
            if (status.getValue() == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all possible mapping of this enum value as lower case strings.
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
