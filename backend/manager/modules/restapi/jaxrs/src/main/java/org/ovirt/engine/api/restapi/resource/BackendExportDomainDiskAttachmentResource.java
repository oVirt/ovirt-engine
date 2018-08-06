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
        DiskAttachment diskAttachment = map(dve);
        parent.addHref(diskAttachment);
        return diskAttachment;
    }
}
