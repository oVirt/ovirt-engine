package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.utils.DiskResourceUtils;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class AbstractBackendStorageDomainDisksResource
        extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk> {

    public static final String UNREGISTERED = "unregistered";

    private final QueryIdResolver<Guid> ID_RESOLVER = new QueryIdResolver<>(
        QueryType.GetDiskByDiskId,
        IdQueryParameters.class
    );

    protected final Guid storageDomainId;

    public AbstractBackendStorageDomainDisksResource(Guid storageDomainId) {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.storageDomainId = storageDomainId;
    }

    public Disks list() {
        boolean unregistered = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, UNREGISTERED, true, false);
        if (unregistered) {
            Guid dataCenterId = BackendDataCenterHelper.lookupByStorageDomainId(this, storageDomainId);
            Disks unregisteredDisks = mapCollection(getBackendCollection(QueryType.GetUnregisteredDisks,
                    new GetUnregisteredDisksQueryParameters(storageDomainId, dataCenterId)));
            unregisteredDisks.getDisks().stream().forEach(d -> d.setActions(null));
            return unregisteredDisks;
        } else {
            return mapCollection(getBackendCollection(QueryType.GetAllDisksByStorageDomainId,
                    new IdQueryParameters(storageDomainId)));
        }
    }

    public Response add(Disk disk) {
        // Unregistering a disk using this method was deprecated in version 4.2
        // unregistered support for add disk has moved to BackendAttachedStorageDomainDiskResource..
        boolean unregistered = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, UNREGISTERED, true, false);
        if (unregistered) {
            // First we need to query the backend to fill in all the information about the disk from the VDSM.
            // We don't just use the information from the Disk object because it's missing a few things like creation
            // date and last modified date.
            Guid dataCenterId = BackendDataCenterHelper.lookupByStorageDomainId(this, storageDomainId);
            GetUnregisteredDiskQueryParameters getDiskParams = new GetUnregisteredDiskQueryParameters(
                    asGuid(disk.getId()), storageDomainId, dataCenterId);
            DiskImage unregisteredDisk =
                    getEntity(DiskImage.class, QueryType.GetUnregisteredDisk, getDiskParams, disk.getId());
            unregisteredDisk =
                    (DiskImage) getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class).map(disk,
                            unregisteredDisk);
            RegisterDiskParameters registerDiskParams = new RegisterDiskParameters(unregisteredDisk, storageDomainId);
            return performCreate(ActionType.RegisterDisk, registerDiskParams, ID_RESOLVER);
        } else {
            validateDiskForCreation(disk);
            AddDiskParameters params = new AddDiskParameters();
            params.setDiskInfo(getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class).map(
                    disk, null));
            params.setStorageDomainId(this.storageDomainId);
            return performCreate(ActionType.AddDisk, params, ID_RESOLVER);
        }
    }

    private void validateDiskForCreation(Disk disk) {
        if (DiskResourceUtils.isLunDisk(disk)) {
            validateParameters(disk.getLunStorage(), 2, "type"); // when creating a LUN disk, user must specify type.
            StorageType storageType = disk.getLunStorage().getType();
            if (storageType != null && storageType == StorageType.ISCSI) {
                validateParameters(disk.getLunStorage().getLogicalUnits().getLogicalUnits().get(0), 3, "address",
                    "target", "port", "id");
            }
        } else if (disk.isSetLunStorage() && (!disk.getLunStorage().isSetLogicalUnits() || !disk.getLunStorage().getLogicalUnits().isSetLogicalUnits())) {
            // TODO: Implement nested entity existence validation infra for validateParameters()
            throw new WebFaultException(
                null,
                localize(Messages.INCOMPLETE_PARAMS_REASON),
                localize(Messages.INCOMPLETE_PARAMS_DETAIL_TEMPLATE, "LogicalUnit", "", "add"),
                Response.Status.BAD_REQUEST
            );
        } else {
            validateParameters(disk, 2, "provisionedSize|size", "format"); // Non lun disks require size and format
        }
    }

    @Override
    protected Disk addParents(Disk disk) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId.toString());
        StorageDomains storageDomains = new StorageDomains();
        storageDomains.getStorageDomains().add(storageDomain);
        disk.setStorageDomain(storageDomain);
        disk.setStorageDomains(storageDomains);
        return disk;
    }

    protected Disks mapCollection(List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities) {
        Disks collection = new Disks();
        for (org.ovirt.engine.core.common.businessentities.storage.Disk disk : entities) {
            collection.getDisks().add(addLinks(populate(map(disk), disk)));
        }
        return collection;
    }
}
