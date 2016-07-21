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

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.resource.DiskAttachmentResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VmDeviceIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendDiskAttachmentResource
        extends AbstractBackendActionableResource<DiskAttachment, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement>
        implements DiskAttachmentResource {

    public static final String DETACH_ONLY = "detach_only";

    private Guid vmId;
    private String diskId;

    protected BackendDiskAttachmentResource(Guid vmId, String diskId) {
        super(diskId, DiskAttachment.class, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class);
        this.vmId = vmId;
        this.diskId = diskId;
    }

    @Override
    public DiskAttachment get() {
        return performGet(VdcQueryType.GetDiskVmElementById, new VmDeviceIdQueryParameters(new VmDeviceId(Guid.createGuidFromString(diskId), vmId)));
    }

    @Override
    public Response remove() {
        boolean detachOnly = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, DETACH_ONLY, true, true);
        if (detachOnly) {
            return performAction(VdcActionType.DetachDiskFromVm, new AttachDetachVmDiskParameters(new DiskVmElement(guid, vmId)));
        }
        else {
            return performAction(VdcActionType.RemoveDisk, new RemoveDiskParameters(guid));
        }
    }

    @Override
    public DiskAttachment update(DiskAttachment attachment) {
        if (attachment.isSetActive()) {
            DiskAttachment attachmentFromDb = get();
            if (!attachmentFromDb.isActive().equals(attachment.isActive())) {
                VdcActionType actionType = attachment.isActive() ? VdcActionType.HotPlugDiskToVm : VdcActionType.HotUnPlugDiskFromVm;
                VmDiskOperationParameterBase params = new VmDiskOperationParameterBase(new DiskVmElement(guid, vmId));
                try {
                    doAction(actionType, params);
                }
                catch (BackendFailureException e) {
                    return handleError(e, false);
                }
            }
        }
        return performUpdate(attachment, new AddDiskResolver(), VdcActionType.UpdateVmDisk, new UpdateParametersProvider());
    }

    private class AddDiskResolver extends EntityIdResolver<Guid> {
        @Override
        public DiskVmElement lookupEntity(Guid id) throws BackendFailureException {
            return getEntity(
                    DiskVmElement.class,
                    VdcQueryType.GetDiskVmElementById,
                    new VmDeviceIdQueryParameters(new VmDeviceId(id, vmId)),
                    id.toString(),
                    false);
        }
    }

    protected class UpdateParametersProvider implements ParametersProvider<DiskAttachment, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement> {
        @Override
        public VdcActionParametersBase getParameters(DiskAttachment incoming, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity) {
            // Disk has to be sent along with the attachment data to the update command
            Disk disk = runQuery(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(Guid.createGuidFromString(diskId))).getReturnValue();

            // If a <disk> was specified inside the attachment data we can update its properties too
            if (incoming.isSetDisk()) {
                disk = DiskMapper.map(incoming.getDisk(), disk);
            }

            DiskVmElement dve = map(incoming, entity);
            dve.getId().setVmId(vmId);
            return new VmDiskOperationParameterBase(dve, disk);
        }
    }
}
