package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.TemplateDiskResource;
import org.ovirt.engine.api.resource.TemplateDisksResource;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateDisksResource
        extends BackendReadOnlyDisksResource
        implements TemplateDisksResource {

    public BackendTemplateDisksResource(Guid parentId, VdcQueryType queryType,
            VdcQueryParametersBase queryParams) {
           super(parentId, queryType, queryParams);
    }

    @Override
    public TemplateDiskResource getDeviceSubResource(String id) {
        return inject(new BackendTemplateDiskResource(asGuidOr404(id), this));
    }

    @Override
    public Disk addParents(Disk disk) {
        // REVISIT: when code refactored in ancestor, won't have to override here
        disk.setTemplate(new Template());
        disk.getTemplate().setId(parentId.toString());
        return disk;
    }
}
