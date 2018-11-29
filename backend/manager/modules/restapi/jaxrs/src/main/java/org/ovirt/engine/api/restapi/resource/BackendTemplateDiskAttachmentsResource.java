package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.TemplateDiskAttachmentResource;
import org.ovirt.engine.api.resource.TemplateDiskAttachmentsResource;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateDiskAttachmentsResource
        extends AbstractBackendCollectionResource<DiskAttachment, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement>
        implements TemplateDiskAttachmentsResource {

    private Guid templateId;

    public BackendTemplateDiskAttachmentsResource(Guid templateId) {
        super(DiskAttachment.class, org.ovirt.engine.core.common.businessentities.storage.DiskVmElement.class);
        this.templateId = templateId;
    }

    @Override
    public DiskAttachments list() {
        return mapCollection(getBackendCollection(QueryType.GetDiskVmElementsByVmId, new IdQueryParameters(templateId)));
    }

    @Override
    public TemplateDiskAttachmentResource getAttachmentResource(String id) {
        return inject(new BackendTemplateDiskAttachmentResource(templateId, id));
    }

    private DiskAttachments mapCollection(List<DiskVmElement> entities) {
        DiskAttachments collection = new DiskAttachments();
        for (org.ovirt.engine.core.common.businessentities.storage.DiskVmElement entity : entities) {
            collection.getDiskAttachments().add(addLinks(populate(map(entity), entity), Template.class));
        }
        return collection;
    }

    @Override
    protected DiskAttachment addParents(DiskAttachment attachment) {
        Template template = new Template();
        template.setId(templateId.toString());
        attachment.setTemplate(template);
        return attachment;
    }
}
