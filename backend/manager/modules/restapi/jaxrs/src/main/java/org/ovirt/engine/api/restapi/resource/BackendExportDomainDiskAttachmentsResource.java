package org.ovirt.engine.api.restapi.resource;

import java.util.stream.Collectors;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.StorageDomain;
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

    @Override
    public DiskAttachments list() {
        DiskAttachments attachments = new DiskAttachments();
        attachments.getDiskAttachments().addAll(parent.getDiskMap().values().stream()
                .map(d -> map(d.getDiskVmElementForVm(parent.vm.getId()))).collect(Collectors.toList()));
        for (DiskAttachment diskAttachment : attachments.getDiskAttachments()) {
            addHref(diskAttachment);
        }
        return attachments;
    }

    /**
     * Manually create links to disk-attachments.
     *
     * Manual creation is necessary due to limitation in link generation infrastructure.
     * The infrastructure identifies a location in the API-tree by the combination:
     * entity-type, parent-type.
     *
     * In this particular case, the combination of disk-attachment under a VM exists
     * twice - once under root, once under Storage-Domain:
     *
     * .../api/vms/xxx/diskattachments
     * .../api/storagedomains/zzz/vms/xxx/diskattachments
     *
     * this causes a bug (https://bugzilla.redhat.com/1374323)
     * Until link-generation infrastructure will be able to handle this, link is
     * created manually
     *
     * @param attachment disk-attachment to add links to
     */
    protected void addHref(DiskAttachment diskAttachment) {
        diskAttachment.getVm().setStorageDomain(new StorageDomain());
        diskAttachment.getVm().getStorageDomain().setId(parent.parent.getStorageDomain().getId().toString());
        addLinks(diskAttachment); //will successfully add the link to the VM
        String vmHref = diskAttachment.getVm().getHref();
        String href = String.join("/", vmHref, "diskattachments", diskAttachment.getId());
        diskAttachment.setHref(href);
    }

    @Override
    public StorageDomainVmDiskAttachmentResource getAttachmentResource(String id) {
        return inject(new BackendExportDomainDiskAttachmentResource(id, this));
    }

    public DiskVmElement getDiskAttachment(Guid id) {
        return parent.getDiskMap().get(id).getDiskVmElementForVm(parent.vm.getId());
    }
}
