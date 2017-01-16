package org.ovirt.engine.ui.frontend.server.dashboard.maps;

import org.ovirt.engine.core.common.businessentities.VMStatus;

/**
 * Map Virtual machine database statuses to one of the following statuses:
 * <ul>
 *   <li>UP</li>
 *   <li>WARNING</li>
 *   <li>DOWN</li>
 * </ul>
 */
public enum VmStatusMap {
    UP(VMStatus.PoweringUp, VMStatus.Up, VMStatus.SavingState, VMStatus.RestoringState, VMStatus.PoweringDown),
    WARNING(VMStatus.Paused, VMStatus.MigratingFrom, VMStatus.MigratingTo, VMStatus.Unknown,
            VMStatus.WaitForLaunch, VMStatus.Suspended, VMStatus.ImageIllegal),
    DOWN(VMStatus.Down, VMStatus.NotResponding, VMStatus.RebootInProgress, VMStatus.ImageLocked);

    private VMStatus[] values;

    private VmStatusMap(VMStatus ...values) {
        this.values = values.clone();
    }

    /**
     * Check if the passed in value maps onto the enum type.
     * @param value An {@code int} value that is based on the index into {@code StoragePoolStatus}
     * @return true if the index maps into this enum value, false otherwise.
     * @see VMStatus
     */
    public boolean isType(int value) {
        for (VMStatus status :values) {
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
