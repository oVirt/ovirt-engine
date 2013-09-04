package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDiskResource extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.Disk> implements DiskResource {

    protected BackendDiskResource(String id) {
        super(id, Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class);
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        QueryIdResolver<Guid> resolver = new QueryIdResolver<Guid>(VdcQueryType.GetDiskByDiskId, IdQueryParameters.class);
        DiskStatisticalQuery query = new DiskStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<Disk, org.ovirt.engine.core.common.businessentities.Disk>(entityType, guid, query));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             Disk.class,
                                                             VdcObjectType.Disk));
    }

    @Override
    public Response doExport(Action action) {
        validateParameters(action, "storageDomain.id|name");
        return doAction(VdcActionType.ExportRepoImage,
                new ExportRepoImageParameters(guid, getStorageDomainId(action)), action);
    }

    @Override
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public Disk get() {
        return performGet(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        return model;
    }
}
