package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

public class SyncLunsInfoForBlockStorageDomainParameters extends StorageDomainParametersBase {

    private static final long serialVersionUID = 6270960404736762805L;

    private List<LUNs> vgInfo;
    private boolean persistNewConnections;

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

    /**
     * When {@code true}, an iSCSI path reported by vdsm that has no matching
     * {@code storage_server_connections} row is persisted (created), mirroring
     * {@code LunHelper#proceedLUNInDb}, so that connections logged into the host ad-hoc
     * (e.g. via the "Login All" button) survive reboot and become cluster-visible.
     * When {@code false} (the default) unmatched paths are skipped with a warning,
     * preserving the long-standing behavior of the activation/periodic sync flows.
     */
    public boolean isPersistNewConnections() {
        return persistNewConnections;
    }

    public void setPersistNewConnections(boolean persistNewConnections) {
        this.persistNewConnections = persistNewConnections;
    }
}
