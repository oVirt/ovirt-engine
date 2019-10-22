/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public class DiskAttachmentMapper {
    @Mapping(from = DiskAttachment.class, to = org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class)
    public static org.ovirt.engine.core.common.businessentities.storage.DiskVmElement map(DiskAttachment model, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement template) {
        org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity = template != null ? template : new DiskVmElement();
        if (model.isSetId()) {
            entity.setId(new VmDeviceId(GuidUtils.asGuid(model.getId()), null));
        } else {
            entity.setId(new VmDeviceId());
        }
        if (model.isSetBootable()) {
            entity.setBoot(model.isBootable());
        }
        if (model.isSetPassDiscard()) {
            entity.setPassDiscard(model.isPassDiscard());
        }
        if (model.isSetInterface()) {
            entity.setDiskInterface(DiskMapper.mapInterface(model.getInterface()));
        }
        if (model.isSetUsesScsiReservation()) {
            entity.setUsingScsiReservation(model.isUsesScsiReservation());
        }
        if (model.isSetReadOnly()) {
            entity.setReadOnly(model.isReadOnly());
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class, to = DiskAttachment.class)
    public static DiskAttachment map(org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity, DiskAttachment template) {
        DiskAttachment model = template != null ? template : new DiskAttachment();
        Guid vmId = entity.getVmId();
        if (vmId != null) {
            Vm vm = new Vm();
            vm.setId(vmId.toString());
            model.setVm(vm);
        }
        Guid diskId = entity.getDiskId();
        if (diskId != null) {
            Disk disk = new Disk();
            disk.setId(diskId.toString());
            model.setDisk(disk);
        }
        model.setId(entity.getDiskId().toString());
        model.setBootable(entity.isBoot());
        model.setPassDiscard(entity.isPassDiscard());
        model.setInterface(DiskMapper.mapInterface(entity.getDiskInterface()));
        model.setActive(entity.isPlugged());
        model.setLogicalName(entity.getLogicalName());
        model.setUsesScsiReservation(entity.isUsingScsiReservation());
        model.setReadOnly(entity.isReadOnly());
        return model;
    }
}
