package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.TemplateDiskResource;
import org.ovirt.engine.api.resource.TemplateDisksResource;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
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
    public Response remove(String id, Action action) {
        getEntity(id);  //will throw 404 if entity not found.
        RemoveDiskParameters params = new RemoveDiskParameters(asGuid(id));
        if (action.isSetForce()) {
            params.setForceDelete(action.isForce());
        }
        if (action.isSetStorageDomain() && action.getStorageDomain().isSetId()) {
            params.setStorageDomainId(asGuid(action.getStorageDomain().getId()));
        }
        return performAction(VdcActionType.RemoveDisk, params);
    }

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveDisk, new RemoveDiskParameters(asGuid(id)));
    }

    @Override
    @SingleEntityResource
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
