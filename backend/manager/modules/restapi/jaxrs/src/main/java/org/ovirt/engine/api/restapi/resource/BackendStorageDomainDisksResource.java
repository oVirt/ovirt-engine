package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainDisksResource extends BackendDisksResource {

    public static final String UNREGISTERED_CONSTRAINT_PARAMETER = "unregistered";

    private final QueryIdResolver<Guid> ID_RESOLVER = new QueryIdResolver<>(VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class);

    Guid storageDomainId;

    public BackendStorageDomainDisksResource(Guid storageDomainId, String... subCollections) {
        super();
        this.storageDomainId = storageDomainId;
    }

    @Override
    public Disks list() {
        boolean unregistered = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, UNREGISTERED_CONSTRAINT_PARAMETER, true, false);
        if (unregistered) {
            return mapCollection(getBackendCollection(VdcQueryType.GetUnregisteredDisks,
                    new GetUnregisteredDisksQueryParameters(storageDomainId, getStoragePoolIdForDomain(storageDomainId))));

        } else {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllDisksByStorageDomainId,
                    new IdQueryParameters(this.storageDomainId)));
        }
    }

    @Override
    public Response add(Disk disk) {
        boolean unregistered = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, UNREGISTERED_CONSTRAINT_PARAMETER, true, false);
        if (unregistered) {
            // First we need to query the backend to fill in all the information about the disk from the VDSM.
            // We don't just use the information from the Disk object because it's missing a few things like creation
            // date and last modified date.
            GetUnregisteredDiskQueryParameters getDiskParams = new GetUnregisteredDiskQueryParameters(
                    asGuid(disk.getId()), storageDomainId, getStoragePoolIdForDomain(storageDomainId));
            DiskImage unregisteredDisk =
                    getEntity(DiskImage.class, VdcQueryType.GetUnregisteredDisk, getDiskParams, disk.getId());
            unregisteredDisk =
                    (DiskImage) getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class).map(disk,
                            unregisteredDisk);
            RegisterDiskParameters registerDiskParams = new RegisterDiskParameters(unregisteredDisk, storageDomainId);
            return performCreate(VdcActionType.RegisterDisk, registerDiskParams, ID_RESOLVER);
        } else {
            validateDiskForCreation(disk);
            AddDiskParameters params = new AddDiskParameters();
            params.setDiskInfo(getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class).map(
                    disk, null));
            params.setStorageDomainId(this.storageDomainId);
            return performCreate(VdcActionType.AddDisk, params, ID_RESOLVER);
        }
    }

    @Override
    public DiskResource getDiskResource(String id) {
        return inject(new BackendStorageDomainDiskResource(id, this));
    }

    @Override
    protected Disk deprecatedPopulate(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        Disk populatedDisk = super.doPopulate(model, entity);

        // this code generates back-link to the corresponding SD
        populatedDisk.setStorageDomain(new StorageDomain());
        populatedDisk.getStorageDomain().setId(this.storageDomainId.toString());

        return model;
    }

    protected Guid getStorageDomainId() {
        return storageDomainId;
    }

    protected Guid getStoragePoolIdForDomain(Guid storageDomainId) {
        // Retrieve the storage pools for the storage domain.
        IdQueryParameters params = new IdQueryParameters(storageDomainId);
        List<StoragePool> storagePools = getBackendCollection(StoragePool.class, VdcQueryType.GetStoragePoolsByStorageDomainId, params);

        if (storagePools != null && !storagePools.isEmpty()) {
            // Take the first storage pool. We should only be running on NFS domains and thus should only have a single
            // storage pool to deal with.
            return storagePools.get(0).getId();
        } else {
            return Guid.Empty;
        }
    }
}
