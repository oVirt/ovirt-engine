package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendVmDisksResource.SUB_COLLECTIONS;

import java.util.Collections;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;


public class BackendVmDiskResource extends BackendDeviceResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.Disk> implements VmDiskResource {
    protected BackendVmDiskResource(String id,
                                  AbstractBackendReadOnlyDevicesResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.Disk> collection,
                                  VdcActionType updateType,
                                  ParametersProvider<Disk, org.ovirt.engine.core.common.businessentities.Disk> updateParametersProvider,
                                  String[] requiredUpdateFields,
                                  String... subCollections) {
        super(Disk.class,
              org.ovirt.engine.core.common.businessentities.Disk.class,
              collection.asGuidOr404(id),
              collection,
              updateType,
              updateParametersProvider,
              requiredUpdateFields,
              SUB_COLLECTIONS);
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver<Guid> resolver = new EntityIdResolver<Guid>() {

            @Override
            public org.ovirt.engine.core.common.businessentities.Disk lookupEntity(
                    Guid guid) throws BackendFailureException {
                return collection.lookupEntity(guid);
            }
        };
        DiskStatisticalQuery query = new DiskStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<Disk, org.ovirt.engine.core.common.businessentities.Disk>(entityType, guid, query));
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        return ((BackendVmDisksResource) collection).doPopulate(model, entity);
    }

    @Override
    protected Disk deprecatedPopulate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        return ((BackendVmDisksResource) collection).deprecatedPopulate(model, entity);
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public Response activate(Action action) {
        HotPlugDiskToVmParameters params =
                new HotPlugDiskToVmParameters(((BackendVmDisksResource) collection).parentId,
                        guid);
        return doAction(VdcActionType.HotPlugDiskToVm, params, action);
    }

    @Override
    public Response deactivate(Action action) {
        HotPlugDiskToVmParameters params =
                new HotPlugDiskToVmParameters(((BackendVmDisksResource) collection).parentId,
                        guid);
        return doAction(VdcActionType.HotUnPlugDiskFromVm, params, action);
    }

    @Override
    public Response move(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid storageDomainId = getStorageDomainId(action);
        Disk disk = getDisk();
        Guid sourceStorageDomainId = getSourceStorageDomainId(disk);
        Guid imageId = asGuid(disk.getImageId());
        MoveDisksParameters params =
                new MoveDisksParameters(Collections.singletonList(new MoveDiskParameters(
                        imageId,
                        sourceStorageDomainId,
                        storageDomainId)));
        return doAction(VdcActionType.MoveDisks, params, action);
    }

    protected Disk getDisk() {
        return performGet(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    protected Guid getSourceStorageDomainId(Disk disk) {
        return asGuid(disk.getStorageDomains().getStorageDomains().get(0).getId());
    }

    @Override
    public Disk get() {
        return super.get();//explicit call solves REST-Easy confusion
    }

    @Override
    protected Disk addLinks(Disk model, String... subCollectionMembersToExclude) {
        return collection.addLinks(model);
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
    public Disk update(Disk resource) {
        validateEnums(Disk.class, resource);
        return super.update(resource);//explicit call solves REST-Easy confusion
    }
}
