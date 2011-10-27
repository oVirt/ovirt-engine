package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.StatisticsResource;
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
}
