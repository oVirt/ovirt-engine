package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendNicsResource.SUB_COLLECTIONS;


public class BackendDiskResource extends BackendDeviceResource<Disk, Disks, DiskImage> implements DiskResource {

    protected BackendDiskResource(String id,
                                  AbstractBackendReadOnlyDevicesResource<Disk, Disks, DiskImage> collection,
                                  VdcActionType updateType,
                                  ParametersProvider<Disk, DiskImage> updateParametersProvider,
                                  String[] requiredUpdateFields,
                                  String... subCollections) {
        super(Disk.class,
              DiskImage.class,
              collection.asGuidOr404(id),
              collection,
              updateType,
              updateParametersProvider,
              requiredUpdateFields,
              SUB_COLLECTIONS);
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver resolver = new EntityIdResolver() {
            public DiskImage lookupEntity(Guid guid) throws BackendFailureException {
                return collection.lookupEntity(guid);
            }
        };
        DiskStatisticalQuery query = new DiskStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<Disk, DiskImage>(entityType, guid, query));
    }

    @Override
    protected Disk populate(Disk model, DiskImage entity) {
        return ((BackendDisksResource)collection).addStatistics(model, entity, uriInfo, httpHeaders);
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public Response activate(Action action) {
        HotPlugDiskToVmParameters params =
                new HotPlugDiskToVmParameters(((BackendDisksResource) collection).parentId,
                        guid);
        return performAction(VdcActionType.HotPlugDiskToVm, params);
    }

    @Override
    public Response deactivate(Action action) {
        HotPlugDiskToVmParameters params =
                new HotPlugDiskToVmParameters(((BackendDisksResource) collection).parentId,
                        guid);
        return performAction(VdcActionType.HotUnPlugDiskFromVm, params);
    }
}
