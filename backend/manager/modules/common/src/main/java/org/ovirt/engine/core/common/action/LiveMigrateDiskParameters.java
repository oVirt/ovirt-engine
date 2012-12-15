package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class LiveMigrateDiskParameters extends MoveOrCopyImageGroupParameters {
    private static final long serialVersionUID = 962820715327420896L;

    public LiveMigrateDiskParameters() {
        // Empty constructor for serializing / deserializing
    }

    public LiveMigrateDiskParameters(Guid imageId, NGuid sourceDomainId, Guid destDomainId, Guid vmId, Guid quotaId) {
        super(imageId, sourceDomainId, destDomainId, ImageOperation.Move);
        setVmId(vmId);
        setQuotaId(quotaId);
    }

    public Guid getSourceStorageDomainId() {
        return getSourceDomainId().getValue();
    }

    public Guid getTargetStorageDomainId() {
        return getStorageDomainId();
    }

    private Guid vmId;

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

}

