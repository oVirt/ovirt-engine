package org.ovirt.engine.api.restapi.types;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.api.model.Checkpoint;
import org.ovirt.engine.api.model.CheckpointState;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class VmCheckpointMapper {
    @Mapping(from = Checkpoint.class, to = VmCheckpoint.class)
    public static VmCheckpoint map(Checkpoint model, VmCheckpoint template) {
        VmCheckpoint entity = (template == null) ? new VmCheckpoint() : template;

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetParentId()) {
            entity.setId(GuidUtils.asGuid(model.getParentId()));
        }
        if (model.isSetDisks()) {
            List<DiskImage> disks = model.getDisks().getDisks().stream()
                    .map(d -> (DiskImage) DiskMapper.map(d, null))
                    .collect(Collectors.toList());
            entity.setDisks(disks);
        }
        return entity;
    }

    @Mapping(from = VmCheckpoint.class, to = Checkpoint.class)
    public static Checkpoint map(VmCheckpoint entity, Checkpoint template) {
        Checkpoint model = (template == null) ? new Checkpoint() : template;

        model.setId(entity.getId().toString());
        if (entity.getVmId() != null) {
            Vm vm = new Vm();
            vm.setId(entity.getVmId().toString());
            model.setVm(vm);
        }
        if (entity.getParentId() != null) {
            model.setParentId(entity.getParentId().toString());
        }
        if (entity.getCreationDate() != null) {
            model.setCreationDate(DateMapper.map(entity.getCreationDate(), null));
        }
        if (entity.getState() != null) {
            model.setState(map(entity.getState()));
        }
        if (entity.getDescription() != null) {
            model.setDescription(entity.getDescription());
        }
        return model;
    }

    public static CheckpointState map(org.ovirt.engine.core.common.businessentities.VmCheckpointState action) {
        switch (action) {
            case CREATED:
                return CheckpointState.CREATED;
            case INVALID:
                return CheckpointState.INVALID;
            default:
                return null;
        }
    }
}
