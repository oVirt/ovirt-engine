package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.compat.Guid;

public class LiveMigrateDiskParameters extends MoveOrCopyImageGroupParameters {
    private static final long serialVersionUID = 962820715327420896L;

    private LiveDiskMigrateStage liveDiskMigrateStage = LiveDiskMigrateStage.IMAGE_PLACEHOLDER_CREATION;

    public LiveMigrateDiskParameters() {
        // Empty constructor for serializing / deserializing
    }

    public LiveMigrateDiskParameters(Guid imageId,
            Guid sourceDomainId,
            Guid destDomainId,
            Guid vmId,
            Guid quotaId,
            Guid diskProfileId,
            Guid imageGroupId) {
        super(imageId, sourceDomainId, destDomainId, ImageOperation.Move);
        setVmId(vmId);
        setQuotaId(quotaId);
        setImageGroupID(imageGroupId);
        setDiskProfileId(diskProfileId);
    }

    public Guid getSourceStorageDomainId() {
        return getSourceDomainId();
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

    public LiveDiskMigrateStage getLiveDiskMigrateStage() {
        return liveDiskMigrateStage;
    }

    public void setLiveDiskMigrateStage(LiveDiskMigrateStage liveDiskMigrateStage) {
        this.liveDiskMigrateStage = liveDiskMigrateStage;
    }

    public enum LiveDiskMigrateStage {
        IMAGE_PLACEHOLDER_CREATION,
        VM_REPLICATE_DISK_START,
        VM_REPLICATE_DISK_FINISH,
        IMAGE_DATA_SYNC_EXEC_START,
        IMAGE_DATA_SYNC_EXEC_END,
        SOURCE_IMAGE_DELETION
    }
}

