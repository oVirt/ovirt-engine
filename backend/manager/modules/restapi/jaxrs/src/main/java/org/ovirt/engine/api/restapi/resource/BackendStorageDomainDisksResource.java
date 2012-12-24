package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetDiskByDiskIdParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;


public class BackendStorageDomainDisksResource extends BackendDisksResource {

    public static final String UNREGISTERED_CONSTRAINT_PARAMETER = "unregistered";

    Guid storageDomainId;

    public BackendStorageDomainDisksResource(Guid storageDomainId, String... subCollections) {
        super();
        this.storageDomainId = storageDomainId;
    }

    @Override
    public Disks list() {
        if (QueryHelper.hasMatrixParam(getUriInfo(), UNREGISTERED_CONSTRAINT_PARAMETER)) {
            // TODO: add "unregistered" disks lookup
            throw new NotImplementedException("\"unregistered\" disks lookup yet not implemented.");
        } else {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllDisksByStorageDomainId,
                    new StorageDomainQueryParametersBase(this.storageDomainId)));
        }
    }

    @Override
    public Response add(Disk disk) {
        validateDiskForCreation(disk);
        AddDiskParameters params = new AddDiskParameters();
        params.setDiskInfo(getMapper(Disk.class,
                                     org.ovirt.engine.core.common.businessentities.Disk.class)
                           .map(disk, null));
        params.setStorageDomainId(this.storageDomainId);
        return performCreation(VdcActionType.AddDisk, params,
                new QueryIdResolver(VdcQueryType.GetDiskByDiskId, GetDiskByDiskIdParameters.class));
    }

    @Override
    protected Response performRemove(String id) {
        RemoveDiskParameters params = new RemoveDiskParameters(asGuid(id));
        params.setStorageDomainId(this.storageDomainId);
        return performAction(VdcActionType.RemoveDisk, params);
    }

    @Override
    public DiskResource getDeviceSubResource(String id) {
        return inject(new BackendStorageDomainDiskResource(id, this.storageDomainId.toString()));
    }

    @Override
    protected Disk populate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        Disk populatedDisk = super.populate(model, entity);

        // this code generates back-link to the corresponding SD
        populatedDisk.setStorageDomain(new StorageDomain());
        populatedDisk.getStorageDomain().setId(this.storageDomainId.toString());

        return model;
    }
}
