package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

public class SyncLunsInfoForBlockStorageDomainParameters extends StorageDomainParametersBase {

    private static final long serialVersionUID = 6270960404736762805L;

    private List<LUNs> vgInfo;

    public SyncLunsInfoForBlockStorageDomainParameters() {
        this(null, null);
    }

    public SyncLunsInfoForBlockStorageDomainParameters(Guid storageDomainId, Guid vdsId) {
        this(storageDomainId, vdsId, null);
    }

    public SyncLunsInfoForBlockStorageDomainParameters(Guid storageDomainId, Guid vdsId, List<LUNs> vgInfo) {
        super(storageDomainId);
        setVdsId(vdsId);
        this.vgInfo = vgInfo;
    }

    public List<LUNs> getVgInfo() {
        return vgInfo;
    }
}
