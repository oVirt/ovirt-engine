package org.ovirt.engine.api.restapi.types;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.api.model.Backup;
import org.ovirt.engine.api.model.BackupPhase;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class VmBackupMapper {
    @Mapping(from = Backup.class, to = VmBackup.class)
    public static VmBackup map(Backup model, VmBackup template) {
        VmBackup entity = template == null ? new VmBackup() : template;
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetFromCheckpointId()) {
            entity.setFromCheckpointId(GuidUtils.asGuid(model.getFromCheckpointId()));
        }
        if (model.isSetToCheckpointId()) {
            entity.setToCheckpointId(GuidUtils.asGuid(model.getToCheckpointId()));
        }
        if (model.isSetPhase()) {
            entity.setPhase(map(model.getPhase()));
        }
        if (model.isSetDisks()) {
            List<DiskImage> disks = model.getDisks().getDisks().stream().map(
                    d -> (DiskImage) DiskMapper.map(d, null)).collect(Collectors.toList());
            entity.setDisks(disks);
        }
        return entity;
    }

    @Mapping(from = VmBackup.class, to = Backup.class)
    public static Backup map(VmBackup entity, Backup template) {
        Backup model = template == null ? new Backup() : template;
        model.setId(entity.getId().toString());
        if (entity.getFromCheckpointId() != null) {
            model.setFromCheckpointId(entity.getFromCheckpointId().toString());
        }
        if (entity.getToCheckpointId() != null) {
            model.setToCheckpointId(entity.getToCheckpointId().toString());
        }
        if (entity.getPhase() != null) {
            model.setPhase(map(entity.getPhase()));
        }
        if (entity.getCreationDate() != null) {
            model.setCreationDate(DateMapper.map(entity.getCreationDate(), null));
        }
        if (entity.getVmId() != null) {
            Vm vm = new Vm();
            vm.setId(entity.getVmId().toString());
            model.setVm(vm);
        }
        return model;
    }

    public static BackupPhase map(org.ovirt.engine.core.common.businessentities.VmBackupPhase action) {
        switch (action) {
        case INITIALIZING:
            return BackupPhase.INITIALIZING;
        case STARTING:
            return BackupPhase.STARTING;
        case READY:
            return BackupPhase.READY;
        case FINALIZING:
            return BackupPhase.FINALIZING;
        default:
            return null;
        }
    }

    public static org.ovirt.engine.core.common.businessentities.VmBackupPhase map(BackupPhase action) {
        switch (action) {
        case INITIALIZING:
            return org.ovirt.engine.core.common.businessentities.VmBackupPhase.INITIALIZING;
        case STARTING:
            return org.ovirt.engine.core.common.businessentities.VmBackupPhase.STARTING;
        case READY:
            return org.ovirt.engine.core.common.businessentities.VmBackupPhase.READY;
        case FINALIZING:
            return org.ovirt.engine.core.common.businessentities.VmBackupPhase.FINALIZING;
        default:
            return null;
        }
    }

}
