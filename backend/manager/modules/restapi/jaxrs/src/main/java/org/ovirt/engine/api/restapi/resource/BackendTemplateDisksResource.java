package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.ReadOnlyDeviceResource;
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
        getEntity(id);  //will throw 404 if entity not found.
        return performAction(VdcActionType.RemoveDisk, new RemoveDiskParameters(asGuid(id)));
    }

    @Override
    @SingleEntityResource
    public ReadOnlyDeviceResource<Disk> getDeviceSubResource(String id) {
        return inject(new BackendReadOnlyDeviceResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.Disk>(modelType, entityType, asGuidOr404(id), this));
    }

}
