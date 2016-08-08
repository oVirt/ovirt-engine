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

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.resource.DiskAttachmentResource;
import org.ovirt.engine.api.resource.DiskAttachmentsResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Disk;
import org.ovirt.engine.api.v3.types.V3Statistics;
import org.ovirt.engine.api.v3.types.V3Status;
import org.ovirt.engine.api.v3.types.V3StorageDomains;
import org.ovirt.engine.api.v3.types.V3VMs;

public class V3DiskOutAdapter implements V3Adapter<Disk, V3Disk> {
    @Override
    public V3Disk adapt(Disk from) {
        V3Disk to = new V3Disk();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetActualSize()) {
            to.setActualSize(from.getActualSize());
        }
        if (from.isSetAlias()) {
            to.setAlias(from.getAlias());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDiskProfile()) {
            to.setDiskProfile(adaptOut(from.getDiskProfile()));
        }
        if (from.isSetFormat()) {
            to.setFormat(from.getFormat().value());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetImageId()) {
            to.setImageId(from.getImageId());
        }
        if (from.isSetInstanceType()) {
            to.setInstanceType(adaptOut(from.getInstanceType()));
        }
        if (from.isSetLogicalName()) {
            to.setLogicalName(from.getLogicalName());
        }
        if (from.isSetLunStorage()) {
            to.setLunStorage(adaptOut(from.getLunStorage()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOpenstackVolumeType()) {
            to.setOpenstackVolumeType(adaptOut(from.getOpenstackVolumeType()));
        }
        if (from.isSetPropagateErrors()) {
            to.setPropagateErrors(from.isPropagateErrors());
        }
        if (from.isSetProvisionedSize()) {
            to.setProvisionedSize(from.getProvisionedSize());

            // In V3 "size" used to be a synonym of "provisioned_size":
            to.setSize(from.getProvisionedSize());
        }
        if (from.isSetQuota()) {
            to.setQuota(adaptOut(from.getQuota()));
        }
        if (from.isSetReadOnly()) {
            to.setReadOnly(from.isReadOnly());
        }
        if (from.isSetSgio()) {
            to.setSgio(from.getSgio().value());
        }
        if (from.isSetShareable()) {
            to.setShareable(from.isShareable());
        }
        if (from.isSetSnapshot()) {
            to.setSnapshot(adaptOut(from.getSnapshot()));
        }
        if (from.isSetSparse()) {
            to.setSparse(from.isSparse());
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new V3Statistics());
            to.getStatistics().getStatistics().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptOut(from.getStorageDomain()));
        }
        if (from.isSetStorageDomains()) {
            to.setStorageDomains(new V3StorageDomains());
            to.getStorageDomains().getStorageDomains().addAll(adaptOut(from.getStorageDomains().getStorageDomains()));
        }
        if (from.isSetStorageType()) {
            to.setStorageType(from.getStorageType().value());
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptOut(from.getTemplate()));
        }
        if (from.isSetUsesScsiReservation()) {
            to.setUsesScsiReservation(from.isUsesScsiReservation());
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        if (from.isSetVms()) {
            to.setVms(new V3VMs());
            to.getVms().getVMs().addAll(adaptOut(from.getVms().getVms()));
        }
        if (from.isSetWipeAfterDelete()) {
            to.setWipeAfterDelete(from.isWipeAfterDelete());
        }

        // In version 4 of the API the interface, bootable and active attributes have been moved from the disk to the
        // disk attachment, as they are specific of the relationship between a particular VM and the disk. But in
        // version 3 of the API we need to continue supporting them. To do so we need to find the disk attachment and
        // copy these attributes to the disk.
        if (to.isSetId() && to.isSetVm() && to.getVm().isSetId()) {
            String diskId = to.getId();
            String vmId = to.getVm().getId();
            VmsResource vmsResource = BackendApiResource.getInstance().getVmsResource();
            VmResource vmResource = vmsResource.getVmResource(vmId);
            DiskAttachmentsResource attachmentsResource = vmResource.getDiskAttachmentsResource();
            DiskAttachmentResource attachmentResource = attachmentsResource.getAttachmentResource(diskId);
            DiskAttachment attachment = attachmentResource.get();
            if (attachment.isSetInterface()) {
                to.setInterface(attachment.getInterface().value());
            }
            if (attachment.isSetBootable()) {
                to.setBootable(attachment.isBootable());
            }
            if (attachment.isSetActive()) {
                to.setActive(attachment.isActive());
            }
            if (attachment.isSetActive()) {
                to.setActive(attachment.isActive());
            }
        }

        return to;
    }
}
