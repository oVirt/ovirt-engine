package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.TemplateDiskResource;
import org.ovirt.engine.api.resource.TemplateDisksResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateDisksResource
        extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements TemplateDisksResource {

    private Guid templateId;

    public BackendTemplateDisksResource(Guid templateId) {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.templateId = templateId;
    }

    @Override
    public Disks list() {
        return mapCollection(getBackendCollection(QueryType.GetVmTemplatesDisks, new IdQueryParameters(templateId)));
    }

    private Disks mapCollection(List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities) {
        Disks collection = new Disks();
        for (org.ovirt.engine.core.common.businessentities.storage.Disk entity : entities) {
            collection.getDisks().add(addLinks(map(entity)));
        }
        return collection;
    }

    @Override
    public TemplateDiskResource getDiskResource(String id) {
        return inject(new BackendTemplateDiskResource(id, templateId));
    }

    @Override
    public Disk addParents(Disk disk) {
        disk.setTemplate(new Template());
        disk.getTemplate().setId(templateId.toString());
        return disk;
    }
}
