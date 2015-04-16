package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SyncImageGroupDataVDSCommandParameters extends TargetDomainImageGroupVDSCommandParameters {

    public static final String SYNC_TYPE_ALL = "ALL";
    public static final String SYNC_TYPE_INTERNAL = "INTERNAL";
    public static final String SYNC_TYPE_LEAF = "LEAF";

    /** The type of sync to be performed - one of the constants above */
    private String syncType;

    public SyncImageGroupDataVDSCommandParameters() {
    }

    public SyncImageGroupDataVDSCommandParameters(Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid dstStorageDomainId,
            String syncType) {
        super(storagePoolId, storageDomainId, imageGroupId, dstStorageDomainId);
        this.syncType = syncType;
    }

    public String getSyncType() {
        return syncType;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("syncType", getSyncType());
    }
}
