package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetDiskByDiskIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDisksResource extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.Disk> implements DisksResource{

    private static final String SUB_COLLECTIONS = "statistics";

    public BackendDisksResource() {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class, SUB_COLLECTIONS);
    }

    @Override
    public Response add(Disk disk) {
        validateParameters(disk, "size", "format", "interface");
        AddDiskParameters params = new AddDiskParameters();
        params.setDiskInfo(getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class).map(disk, null));
        return performCreation(VdcActionType.AddDisk, params,
                new QueryIdResolver(VdcQueryType.GetDiskByDiskId, GetDiskByDiskIdParameters.class));
    }

    @Override
    public Disks list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllDisks, new VdcQueryParametersBase()));
    }

    @Override
    public DiskResource getDeviceSubResource(String id) {
        return inject(new BackendDiskResource(id));
    }

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveDisk, new RemoveDiskParameters(Guid.createGuidFromString(id)));
    }

    protected Disks mapCollection(List<org.ovirt.engine.core.common.businessentities.Disk> entities) {
        Disks collection = new Disks();
        for (org.ovirt.engine.core.common.businessentities.Disk disk : entities) {
            collection.getDisks().add(addLinks(map(disk)));
        }
        return collection;
    }
}
