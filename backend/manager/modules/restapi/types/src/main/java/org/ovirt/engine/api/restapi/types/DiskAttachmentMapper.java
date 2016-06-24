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
            entity.setDiskInterface(DiskMapper.mapInterface(model.getInterface()));
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class, to = DiskAttachment.class)
    public static DiskAttachment map(org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity, DiskAttachment template) {
        DiskAttachment model = template != null ? template : new DiskAttachment();
        model.setId(entity.getDiskId().toString());
        model.setBootable(entity.isBoot());
        model.setInterface(DiskMapper.mapInterface(entity.getDiskInterface()));
        return model;
    }
}
