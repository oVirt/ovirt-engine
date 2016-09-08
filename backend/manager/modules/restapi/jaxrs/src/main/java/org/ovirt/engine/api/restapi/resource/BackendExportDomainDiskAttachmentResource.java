package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.resource.StorageDomainVmDiskAttachmentResource;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;

public class BackendExportDomainDiskAttachmentResource
        extends AbstractBackendSubResource<DiskAttachment, DiskVmElement>
        implements StorageDomainVmDiskAttachmentResource {

    private final BackendExportDomainDiskAttachmentsResource parent;
    private final String attachmentId;

    public BackendExportDomainDiskAttachmentResource(String attachmentId, BackendExportDomainDiskAttachmentsResource parent) {
        super(attachmentId, DiskAttachment.class, DiskVmElement.class);
        this.parent = parent;
        this.attachmentId = attachmentId;
    }

    @Override
    public DiskAttachment get() {
        DiskVmElement dve = parent.getDiskAttachment(asGuid(attachmentId));
        if (dve == null) {
            return notFound();
        }
        // TODO: Currently we don't add links as the they are wrongly pointing to /vms/{vm_id}/diskattachemnts/{attachment_id}
        // instead of /storagedomains/{storage_id}/vms/{vm_id}/diskattachments/{attachment_id}
        // this needs to be added once the problem is solved
        return map(dve);
    }
}
