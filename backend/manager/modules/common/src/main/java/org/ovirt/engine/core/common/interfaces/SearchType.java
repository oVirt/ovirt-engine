package org.ovirt.engine.core.common.interfaces;

import java.io.Serializable;

public enum SearchType implements Serializable {
    VM,
    VDS,
    VmTemplate,
    AuditLog,
    DirectoryUser,
    DirectoryGroup,
    DBUser,
    DBGroup,
    VmPools,
    Cluster,
    StoragePool,
    StorageDomain,
    Quota,
    Disk,
    GlusterVolume,
    Network,
    IscsiBond,
    Provider,
    InstanceType,
    ImageType,
    ImageTransfer,
    Session,
    Job,
    VnicProfile;

    public int getValue() {
        return this.ordinal();
    }

    public static SearchType forValue(int value) {
        return values()[value];
    }
}
