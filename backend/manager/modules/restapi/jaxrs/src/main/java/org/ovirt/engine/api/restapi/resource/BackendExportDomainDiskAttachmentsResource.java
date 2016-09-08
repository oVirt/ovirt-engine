package org.ovirt.engine.api.restapi.resource;

import java.util.stream.Collectors;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.resource.StorageDomainVmDiskAttachmentResource;
import org.ovirt.engine.api.resource.StorageDomainVmDiskAttachmentsResource;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public class BackendExportDomainDiskAttachmentsResource
        extends AbstractBackendCollectionResource<DiskAttachment, DiskVmElement>
        implements StorageDomainVmDiskAttachmentsResource {

    private BackendStorageDomainVmResource parent;

    public BackendExportDomainDiskAttachmentsResource(BackendStorageDomainVmResource parent) {
        super(DiskAttachment.class, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class);
        this.parent = parent;
    }

    // TODO: Currently we don't add links as the they are wrongly pointing to /vms/{vm_id}/diskattachemnts
    // instead of /storagedomains/{storage_id}/vms/{vm_id}/diskattachments
    // this needs to be added once the problem is solved
    @Override
    public DiskAttachments list() {
        DiskAttachments attachments = new DiskAttachments();
        attachments.getDiskAttachments().addAll(parent.getDiskMap().values().stream()
                .map(d -> map(d.getDiskVmElementForVm(parent.vm.getId()))).collect(Collectors.toList()));
        return attachments;
    }

    @Override
    public StorageDomainVmDiskAttachmentResource getAttachmentResource(String id) {
        return inject(new BackendExportDomainDiskAttachmentResource(id, this));
    }

    public DiskVmElement getDiskAttachment(Guid id) {
        return parent.getDiskMap().get(id).getDiskVmElementForVm(parent.vm.getId());
    }
}
