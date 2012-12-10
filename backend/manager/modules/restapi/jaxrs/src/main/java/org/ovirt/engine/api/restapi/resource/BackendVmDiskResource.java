package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendNicsResource.SUB_COLLECTIONS;


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
    protected Disk populate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        return ((BackendVmDisksResource)collection).addStatistics(model, entity, uriInfo, httpHeaders);
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
    public Disk get() {
        return super.get();//explicit call solves REST-Easy confusion
    }

    @Override
    public Disk update(Disk resource) {
        validateEnums(Disk.class, resource);
        return super.update(resource);//explicit call solves REST-Easy confusion
    }
}
