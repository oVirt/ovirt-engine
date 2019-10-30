/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.DiskAttachmentResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
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
        DiskAttachment diskAttachment = performGet(QueryType.GetDiskVmElementById, new VmDeviceIdQueryParameters(new VmDeviceId(Guid.createGuidFromString(diskId), vmId)), Vm.class);
        /*
         * Href of the diskattachment must be set manually due to a bug (https://bugzilla.redhat.com/1647018).
         * The bug is the result of an exceptional case where the same entity (disk-attachment)
         * has the same parent (vm) in 2 different locations in the API, causing ambiguity
         * in the link generation process. Enhancing the infrastructure to deal with such cases
         * in a generic way is complex and is not justified by this single occurrence, hence the
         * hardly-typed solution.
         */
        diskAttachment.setHref("/ovirt-engine/api/vms/" + vmId.toString() + "/diskattachments/" + diskAttachment.getId());
        return diskAttachment;
    }

    @Override
    public Response remove() {
        boolean detachOnly = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, DETACH_ONLY, true, true);
        if (detachOnly) {
            return performAction(ActionType.DetachDiskFromVm, new AttachDetachVmDiskParameters(new DiskVmElement(guid, vmId)));
        } else {
            return performAction(ActionType.RemoveDisk, new RemoveDiskParameters(guid));
        }
    }

    @Override
    public DiskAttachment update(DiskAttachment attachment) {
        if (attachment.isSetActive()) {
            DiskAttachment attachmentFromDb = get();
            if (!attachmentFromDb.isActive().equals(attachment.isActive())) {
                ActionType actionType = attachment.isActive() ? ActionType.HotPlugDiskToVm : ActionType.HotUnPlugDiskFromVm;
                VmDiskOperationParameterBase params = new VmDiskOperationParameterBase(new DiskVmElement(guid, vmId));
                try {
                    doAction(actionType, params);
                } catch (BackendFailureException e) {
                    return handleError(e, false);
                }
            }
        }
        DiskAttachment diskAttachment = performUpdate(attachment, new AddDiskResolver(), ActionType.UpdateDisk, new UpdateParametersProvider());
        /*
         * Href of the diskattachment must be fixed manually due to a bug (https://bugzilla.redhat.com/1647018).
         * The bug is the result of an exceptional case where the same entity (disk-attachment)
         * has the same parent (vm) in 2 different locations in the API, causing ambiguity
         * in the link generation process. Enhancing the infrastructure to deal with such cases
         * in a generic way is complex and is not justified by this single occurrence, hence the
         * hardly-typed solution.
         */
        diskAttachment.setHref(diskAttachment.getHref().replace("/null/", "/diskattachments/"));
        return diskAttachment;
    }

    private class AddDiskResolver extends EntityIdResolver<Guid> {
        @Override
        public DiskVmElement lookupEntity(Guid id) throws BackendFailureException {
            return getEntity(
                    DiskVmElement.class,
                    QueryType.GetDiskVmElementById,
                    new VmDeviceIdQueryParameters(new VmDeviceId(id, vmId)),
                    id.toString(),
                    false);
        }
    }

    protected class UpdateParametersProvider implements ParametersProvider<DiskAttachment, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement> {
        @Override
        public UpdateDiskParameters getParameters(DiskAttachment incoming, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity) {
            DiskVmElement dve = map(incoming, entity);
            dve.getId().setVmId(vmId);

            // Disk has to be sent along with the attachment data to the update command
            Disk disk = runQuery(QueryType.GetDiskByDiskId, new IdQueryParameters(Guid.createGuidFromString(diskId))).getReturnValue();

            // If a <disk> was specified inside the attachment data we can update its properties too
            if (incoming.isSetDisk()) {
                disk = DiskMapper.map(incoming.getDisk(), disk);

                // TODO: Remove this in version 5 of the API as the setting of the read only attribute should be from DiskAttachment
                if (incoming.getDisk().isSetReadOnly()) {
                    dve.setReadOnly(incoming.getDisk().isReadOnly());
                }
            }

            return new UpdateDiskParameters(dve, disk);
        }
    }
}
