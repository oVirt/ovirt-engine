package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskType;
import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;

public abstract class VmBackupConfigVDSCommandBase<P extends VmBackupVDSParameters> extends VdsBrokerCommand<P> {
    @Inject
    private VmCheckpointDao vmCheckpointDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    private Set<Guid> vmCheckpointDisksIds;

    public VmBackupConfigVDSCommandBase(P parameters) {
        super(parameters);
    }

    protected abstract String getDiskBackupMode(DiskImage diskImage);

    private HashMap[] createDisksMap(Guid toCheckpointId) {
        return getParameters().getVmBackup().getDisks().stream().map(diskImage -> {
            Map<String, Object> imageParams = new HashMap<>();
            imageParams.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
            imageParams.put(VdsProperties.ImageId, diskImage.getId().toString());
            imageParams.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
            imageParams.put(VdsProperties.CHECKPOINT, isDiskInCheckpoint(diskImage.getImageId(), toCheckpointId));
            String backupMode = getDiskBackupMode(diskImage);
            if (backupMode != null) {
                imageParams.put(VdsProperties.BACKUP_MODE, backupMode);
            }

            Map<Guid, String> scratchDisksMap = getParameters().getScratchDisksMap();
            if (scratchDisksMap != null && scratchDisksMap.containsKey(diskImage.getId())) {
                Map<String, Object> scratchDiskParams = new HashMap<>();
                scratchDiskParams.put(VdsProperties.Path, scratchDisksMap.get(diskImage.getId()));
                StorageDomainStatic sourceDomain = storageDomainStaticDao.get(diskImage.getStorageIds().get(0));
                DiskType diskType = sourceDomain.getStorageType().isBlockDomain() ? DiskType.Block : DiskType.File;
                scratchDiskParams.put(VdsProperties.Type, diskType.getName());
                imageParams.put(VdsProperties.SCRATCH_DISK, scratchDiskParams);
            }
            return imageParams;
        }).toArray(HashMap[]::new);
    }

    protected Map<String, Object> createBackupConfig() {
        Guid fromCheckpointId = getParameters().getVmBackup().getFromCheckpointId();
        Guid toCheckpointId = getParameters().getVmBackup().getToCheckpointId();

        Map<String, Object> backupConfig = new HashMap<>();
        backupConfig.put("backup_id", getParameters().getVmBackup().getId().toString());
        backupConfig.put("disks", createDisksMap(toCheckpointId));
        backupConfig.put("from_checkpoint_id", fromCheckpointId != null ? fromCheckpointId.toString() : null);
        backupConfig.put("to_checkpoint_id", toCheckpointId != null ? toCheckpointId.toString() : null);
        backupConfig.put("parent_checkpoint_id", getParentId());
        backupConfig.put("require_consistency", getParameters().isRequireConsistency());

        return backupConfig;
    }

    private boolean isDiskInCheckpoint(Guid diskImageId, Guid toCheckpointId) {
        return toCheckpointId != null && getVmCheckpointDisksIds(toCheckpointId).contains(diskImageId);
    }

    private Set<Guid> getVmCheckpointDisksIds(Guid toCheckpointId) {
        if (vmCheckpointDisksIds == null) {
            vmCheckpointDisksIds = vmCheckpointDao.getDisksByCheckpointId(toCheckpointId).stream()
                    .map(DiskImage::getImageId)
                    .collect(Collectors.toSet());
        }
        return vmCheckpointDisksIds;
    }

    private String getParentId() {
        Guid toCheckpointId = getParameters().getVmBackup().getToCheckpointId();
        if (toCheckpointId != null) {
            VmCheckpoint toCheckpoint = vmCheckpointDao.get(toCheckpointId);
            if (toCheckpoint != null) {
                return toCheckpoint.getParentId() != null ? toCheckpoint.getParentId().toString() : null;
            }
        }
        return null;
    }
}
