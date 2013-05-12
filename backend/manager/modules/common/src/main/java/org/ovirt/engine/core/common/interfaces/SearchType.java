package org.ovirt.engine.core.common.interfaces;

import java.io.Serializable;

public enum SearchType implements Serializable {
    VM,
    VDS,
    VmTemplate,
    AuditLog,
    AdUser,
    AdGroup,
    DBUser,
    VmPools,
    Cluster,
    StoragePool,
    StorageDomain,
    Quota,
    Disk,
    GlusterVolume,
    Network,
    Provider;

    public int getValue() {
        return this.ordinal();
    }

    public static SearchType forValue(int value) {
        return values()[value];
    }
}
