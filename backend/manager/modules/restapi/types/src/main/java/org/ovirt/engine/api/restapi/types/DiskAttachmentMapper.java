/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;

public class DiskAttachmentMapper {
    @Mapping(from = DiskAttachment.class, to = org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class)
    public static org.ovirt.engine.core.common.businessentities.storage.DiskVmElement map(DiskAttachment model, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement template) {
        org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity = template != null ? template : new DiskVmElement();
        if (model.isSetId()) {
            entity.setId(new VmDeviceId(GuidUtils.asGuid(model.getId()), null));
        }
        else {
            entity.setId(new VmDeviceId());
        }
        if (model.isSetBootable()) {
            entity.setBoot(model.isBootable());
        }
        if (model.isSetInterface()) {
            entity.setDiskInterface(map(model.getInterface(), null));
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class, to = DiskAttachment.class)
    public static DiskAttachment map(org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity, DiskAttachment template) {
        DiskAttachment model = template != null ? template : new DiskAttachment();
        model.setId(entity.getDiskId().toString());
        model.setBootable(entity.isBoot());
        model.setInterface(map(entity.getDiskInterface(), null));
        return model;
    }

    @Mapping(from = DiskInterface.class, to = org.ovirt.engine.core.common.businessentities.storage.DiskInterface.class)
    public static org.ovirt.engine.core.common.businessentities.storage.DiskInterface map(
            DiskInterface diskInterface,
            org.ovirt.engine.core.common.businessentities.storage.DiskInterface template) {
        switch (diskInterface) {
        case IDE:
            return org.ovirt.engine.core.common.businessentities.storage.DiskInterface.IDE;
        case VIRTIO:
            return org.ovirt.engine.core.common.businessentities.storage.DiskInterface.VirtIO;
        case VIRTIO_SCSI:
            return org.ovirt.engine.core.common.businessentities.storage.DiskInterface.VirtIO_SCSI;
        case SPAPR_VSCSI:
            return org.ovirt.engine.core.common.businessentities.storage.DiskInterface.SPAPR_VSCSI;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.DiskInterface.class, to = DiskInterface.class)
    public static DiskInterface map(org.ovirt.engine.core.common.businessentities.storage.DiskInterface diskInterface, DiskInterface template) {
        switch (diskInterface) {
        case IDE:
            return DiskInterface.IDE;
        case VirtIO:
            return DiskInterface.VIRTIO;
        case VirtIO_SCSI:
            return DiskInterface.VIRTIO_SCSI;
        case SPAPR_VSCSI:
            return DiskInterface.SPAPR_VSCSI;
        default:
            return null;
        }
    }
}
