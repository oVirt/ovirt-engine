package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.api.resource.MovableCopyableDiskResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.utils.DiskResourceUtils;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDisksResource extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.Disk> implements DisksResource{

    static final String[] SUB_COLLECTIONS = { "permissions", "statistics" };
    public BackendDisksResource() {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class, SUB_COLLECTIONS);
    }

    @Override
    public Response add(Disk disk) {
        validateDiskForCreation(disk);
        AddDiskParameters params = new AddDiskParameters();
        params.setDiskInfo(getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class).map(disk, null));
        if (disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains() && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
            params.setStorageDomainId(Guid.createGuidFromStringDefaultEmpty(disk.getStorageDomains().getStorageDomains().get(0).getId()));
        } else if (disk.isSetStorageDomains() && disk.getStorageDomains().getStorageDomains().get(0).isSetName()) {
            Guid storageDomainId = getStorageDomainId(disk.getStorageDomains().getStorageDomains().get(0).getName());
            if (storageDomainId == null) {
                notFound(StorageDomain.class);
            } else {
                params.setStorageDomainId(storageDomainId);
            }
        }
        if (disk.isSetLunStorage() && disk.getLunStorage().isSetHost()) {
            params.setVdsId(getHostId(disk.getLunStorage().getHost()));
        }
        return performCreate(VdcActionType.AddDisk, params,
                new QueryIdResolver<Guid>(VdcQueryType.GetDiskByDiskId, IdQueryParameters.class));
    }

    protected void validateDiskForCreation(Disk disk) {
        validateParameters(disk, 2, "interface");
        if (DiskResourceUtils.isLunDisk(disk)) {
            validateParameters(disk.getLunStorage(), 3, "type"); // when creating a LUN disk, user must specify type.
            StorageType storageType = StorageType.fromValue(disk.getLunStorage().getType());
            if (storageType != null && storageType == StorageType.ISCSI) {
                validateParameters(disk.getLunStorage().getLogicalUnits().get(0), 3, "address", "target", "port", "id");
            }
        } else if (disk.isSetLunStorage() && disk.getLunStorage().getLogicalUnits().isEmpty()) {
            // TODO: Implement nested entity existence validation infra for validateParameters()
            throw new WebFaultException(null,
                                        localize(Messages.INCOMPLETE_PARAMS_REASON),
                                        localize(Messages.INCOMPLETE_PARAMS_DETAIL_TEMPLATE, "LogicalUnit", "", "add"),
                                        Response.Status.BAD_REQUEST);
        } else {
            validateParameters(disk, 2, "provisionedSize|size", "format"); // Non lun disks require size and format
        }
        validateEnums(Disk.class, disk);
    }

    private Guid getStorageDomainId(String storageDomainName) {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> storageDomains =
                getBackendCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                        VdcQueryType.GetAllStorageDomains,
                        new VdcQueryParametersBase());
        for (org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageName().equals(storageDomainName)) {
                return storageDomain.getId();
            }
        }
        return null;
    }

    @Override
    public Disks list() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllDisks, new VdcQueryParametersBase()));
        } else {
            return mapCollection(getBackendCollection(SearchType.Disk));
        }
    }

    @Override
    @SingleEntityResource
    public MovableCopyableDiskResource getDeviceSubResource(String id) {
        return inject(new BackendDiskResource(id));
    }

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveDisk, new RemoveDiskParameters(asGuid(id)));
    }

    protected Disks mapCollection(List<org.ovirt.engine.core.common.businessentities.Disk> entities) {
        Disks collection = new Disks();
        for (org.ovirt.engine.core.common.businessentities.Disk disk : entities) {
            collection.getDisks().add(addLinks(populate(map(disk), disk)));
        }
        return collection;
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        return model;
    }
}
