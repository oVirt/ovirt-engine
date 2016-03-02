package org.ovirt.engine.core.bll.scheduling.utils;

import org.ovirt.engine.core.bll.scheduling.pending.PendingResource;

public class VmSpecificPendingResourceEqualizer {
    public static boolean isEqual(PendingResource source, Object other) {
        if (source == other) {
            return true;
        }

        if (other == null || source.getClass() != other.getClass()) {
            return false;
        }

        return source.getVm().equals(((PendingResource) other).getVm());
    }

    public static int calcHashCode(PendingResource source) {
        return source.getVm().hashCode();
    }
}
