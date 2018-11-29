package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.TemplateDiskAttachmentResource;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VmDeviceIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateDiskAttachmentResource
        extends AbstractBackendActionableResource<DiskAttachment, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement>
        implements TemplateDiskAttachmentResource {

    private Guid templateId;
    private String diskId;

    protected BackendTemplateDiskAttachmentResource(Guid templateId, String diskId) {
        super(diskId, DiskAttachment.class, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class);
        this.templateId = templateId;
        this.diskId = diskId;
    }

    @Override
    public DiskAttachment get() {
        return performGet(QueryType.GetDiskVmElementById, new VmDeviceIdQueryParameters(new VmDeviceId(Guid.createGuidFromString(diskId), templateId)), Template.class);
    }

    @Override
    public Response remove() {
        return getTemplateDiskResource().remove();
    }

    private BackendTemplateDiskResource getTemplateDiskResource() {
        return inject(new BackendTemplateDiskResource(diskId, templateId));
    }

    @Override
    protected DiskAttachment addParents(DiskAttachment attachment) {
        Template template = new Template();
        template.setId(templateId.toString());
        attachment.setTemplate(template);
        return attachment;
    }
}
