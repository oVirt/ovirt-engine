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

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.BooleanUtils;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.resource.DiskAttachmentResource;
import org.ovirt.engine.api.resource.DiskAttachmentsResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.utils.DiskResourceUtils;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VmDeviceIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendDiskAttachmentsResource
        extends AbstractBackendCollectionResource<DiskAttachment, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement>
        implements DiskAttachmentsResource {

    private Guid vmId;

    public BackendDiskAttachmentsResource(Guid vmId) {
        super(DiskAttachment.class, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class);
        this.vmId = vmId;
    }

    @Override
    public DiskAttachments list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetDiskVmElementsByVmId, new IdQueryParameters(vmId)));
    }

    @Override
    public Response add(DiskAttachment attachment) {
        if (attachment.isSetDisk() && attachment.getDisk().isSetId()) {
            Guid diskId = Guid.createGuidFromStringDefaultEmpty(attachment.getDisk().getId());
            return attachDiskToVm(this, attachment, new AttachDiskResolver(diskId, vmId));
        }
        else {
            return createDisk(this, attachment, new AddDiskResolver());
        }
    }

    protected Response createDisk(AbstractBackendCollectionResource resource, DiskAttachment attachment, IResolver entityResolver) {
        Disk disk = attachment.getDisk();
        validateDiskForCreation(disk);
        updateStorageTypeForDisk(disk);
        return resource.performCreate(VdcActionType.AddDisk,
                getAddParameters(attachment, DiskMapper.map(disk, null), disk), entityResolver);
    }

    @Override
    public DiskAttachmentResource getAttachmentResource(String id) {
        return inject(new BackendDiskAttachmentResource(vmId, id));
    }

    protected Response attachDiskToVm(AbstractBackendCollectionResource resource, DiskAttachment attachment, IResolver entityResolver) {
        Guid diskId = Guid.createGuidFromStringDefaultEmpty(attachment.getDisk().getId());

        DiskVmElement dve = map(attachment);
        dve.getId().setVmId(vmId);
        dve.getId().setDeviceId(diskId);

        AttachDetachVmDiskParameters params = new AttachDetachVmDiskParameters(dve);

        Disk disk = attachment.getDisk();

        boolean isDiskActive = false;
        if (attachment.isSetActive()) {
            isDiskActive = BooleanUtils.toBoolean(attachment.isActive());
        }
        params.setPlugUnPlug(isDiskActive);

        boolean isDiskReadOnly = false;
        if (disk.isSetReadOnly()) {
            isDiskReadOnly = BooleanUtils.toBoolean(disk.isReadOnly());
        }
        params.setReadOnly(isDiskReadOnly);

        if (disk.isSetSnapshot()) {
            validateParameters(disk, "snapshot.id");
            params.setSnapshotId(asGuid(disk.getSnapshot().getId()));
        }

        return resource.performCreate(VdcActionType.AttachDiskToVm, params, entityResolver);
    }

    private class AttachDiskResolver implements IResolver<VmDeviceId, DiskVmElement> {
        private Guid diskId;
        private Guid vmId;
        public AttachDiskResolver(Guid diskId, Guid vmId) {
            this.diskId = diskId;
            this.vmId = vmId;
        }
        @Override
        public DiskVmElement resolve(VmDeviceId id) throws BackendFailureException {
            VmDeviceId vmDeviceId = new VmDeviceId(diskId, vmId);
            return getEntity(
                    DiskVmElement.class,
                    VdcQueryType.GetDiskVmElementById,
                    new VmDeviceIdQueryParameters(vmDeviceId),
                    vmDeviceId.getDeviceId().toString(),
                    true
            );
        }
    }

    protected void validateDiskForCreation(Disk disk) {
        validateParameters(disk);
        if (DiskResourceUtils.isLunDisk(disk)) {
            validateParameters(disk.getLunStorage(), 4, "type"); // when creating a LUN disk, user must specify type.
            StorageType storageType = disk.getLunStorage().getType();
            if (storageType != null && storageType == StorageType.ISCSI) {
                validateParameters(disk.getLunStorage().getLogicalUnits().getLogicalUnits().get(0), 4, "address", "target", "port", "id");
            }
        } else if (disk.isSetLunStorage() && (!disk.getLunStorage().isSetLogicalUnits() || !disk.getLunStorage().getLogicalUnits().isSetLogicalUnits())) {
            // TODO: Implement nested entity existence validation infra for validateParameters()
            throw new WebFaultException(null,
                    localize(Messages.INCOMPLETE_PARAMS_REASON),
                    localize(Messages.INCOMPLETE_PARAMS_DETAIL_TEMPLATE, "LogicalUnit", "", "add"),
                    Response.Status.BAD_REQUEST);
        } else {
            validateParameters(disk, 4, "provisionedSize|size", "format"); // Non lun disks require size and format
        }
    }

    protected void updateStorageTypeForDisk(Disk disk) {
        Guid storageDomainId = getStorageDomainId(disk);
        if (storageDomainId != null) {
            org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain = getStorageDomainById(storageDomainId);
            if (storageDomain != null) {
                disk.setStorageType(DiskMapper.map(storageDomain.getStorageDomainType()));
            }
        }
    }

    private VdcActionParametersBase getAddParameters(DiskAttachment attachment, org.ovirt.engine.core.common.businessentities.storage.Disk entity, Disk disk) {
        DiskVmElement dve = map(attachment);
        dve.getId().setVmId(vmId);
        AddDiskParameters parameters = new AddDiskParameters(dve, entity);
        Guid storageDomainId = getStorageDomainId(disk);
        if (storageDomainId != null) {
            parameters.setStorageDomainId(storageDomainId);
        }
        if (attachment.isSetActive()) {
            parameters.setPlugDiskToVm(attachment.isActive());
        }
        if (disk.isSetLunStorage() && disk.getLunStorage().isSetHost()) {
            parameters.setVdsId(getHostId(disk.getLunStorage().getHost()));
        }
        return parameters;
    }

    private Guid getStorageDomainId(Disk disk) {
        if (disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains()
                && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
            return asGuid(disk.getStorageDomains().getStorageDomains().get(0).getId());
        } else if (disk.isSetStorageDomains() && disk.getStorageDomains().getStorageDomains().get(0).isSetName()) {
            Guid storageDomainId = getStorageDomainIdByName(disk.getStorageDomains().getStorageDomains().get(0).getName());
            if (storageDomainId == null) {
                notFound(StorageDomain.class);
            } else {
                return storageDomainId;
            }
        }
        return null;
    }

    private Guid getStorageDomainIdByName(String storageDomainName) {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> storageDomains =
                getBackendCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                        VdcQueryType.GetAllStorageDomains,
                        new VdcQueryParametersBase());
        for (org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageName().equals(storageDomainName)) {
                return storageDomain.getId();
            }
        }
        return null;
    }

    private class AddDiskResolver implements IResolver<Guid, DiskVmElement> {
        @Override
        public DiskVmElement resolve(Guid id) throws BackendFailureException {
            return getEntity(
                    DiskVmElement.class,
                    VdcQueryType.GetDiskVmElementById,
                    new VmDeviceIdQueryParameters(new VmDeviceId(id, vmId)),
                    id.toString(),
                    true
            );
        }
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomainById(Guid id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class, VdcQueryType.GetStorageDomainById, new IdQueryParameters(id), id.toString());
    }

    private DiskAttachments mapCollection(List<DiskVmElement> entities) {
        DiskAttachments collection = new DiskAttachments();
        for (org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity : entities) {
            collection.getDiskAttachments().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }
}
