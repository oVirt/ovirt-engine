package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.utils.DiskResourceUtils;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDisksResource
        extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements DisksResource {

    public BackendDisksResource() {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
    }

    @Override
    public Response add(Disk disk) {
        validateDiskForCreation(disk);
        AddDiskParameters params = new AddDiskParameters();
        Guid storageDomainId = getStorageDomainId(disk);
        params.setStorageDomainId(storageDomainId);
        if (storageDomainId != null) {
            updateStorageTypeForDisk(disk, storageDomainId);
        }
        params.setDiskInfo(getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class).map(disk, null));
        if (disk.isSetLunStorage() && disk.getLunStorage().isSetHost()) {
            params.setVdsId(getHostId(disk.getLunStorage().getHost()));
        }
        if (disk.isSetId()) {
            params.setUsePassedDiskId(true);
        }
        if (disk.isSetImageId()) {
            params.setUsePassedImageId(true);
        }
        return performCreate(ActionType.AddDisk, params,
                new QueryIdResolver<Guid>(QueryType.GetDiskByDiskId, IdQueryParameters.class));
    }

    private void updateStorageTypeForDisk(Disk disk, Guid storageDomainId) {
        org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain = getStorageDomainById(storageDomainId);
        if (storageDomain != null) {
            disk.setStorageType(DiskMapper.map(storageDomain.getStorageDomainType()));
        }
    }

    private Guid getStorageDomainId(Disk disk) {
        if (disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains()
                && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
            return asGuid(disk.getStorageDomains().getStorageDomains().get(0).getId());
        } else if (disk.isSetStorageDomains() && disk.getStorageDomains().getStorageDomains().get(0).isSetName()) {
            String storageName = disk.getStorageDomains().getStorageDomains().get(0).getName();
            Guid storageDomainId = getStorageDomainIdByName(storageName);
            if (storageDomainId == null) {
                notFound(storageName);
            } else {
                return storageDomainId;
            }
        }
        return null;
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomainById(Guid id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class, QueryType.GetStorageDomainById, new IdQueryParameters(id), id.toString());
    }

    protected void validateDiskForCreation(Disk disk) {
        if (DiskResourceUtils.isLunDisk(disk)) {
            validateParameters(disk.getLunStorage(), 2, "type"); // when creating a LUN disk, user must specify type.
            StorageType storageType = disk.getLunStorage().getType();
            if (storageType != null && storageType == StorageType.ISCSI) {
                validateParameters(disk.getLunStorage().getLogicalUnits().getLogicalUnits().get(0), 3, "address",
                        "target", "port", "id");
            }
        } else if (disk.isSetLunStorage() && (!disk.getLunStorage().isSetLogicalUnits() || !disk.getLunStorage().getLogicalUnits().isSetLogicalUnits())) {
            // TODO: Implement nested entity existence validation infra for validateParameters()
            throw new WebFaultException(null,
                                        localize(Messages.INCOMPLETE_PARAMS_REASON),
                                        localize(Messages.INCOMPLETE_PARAMS_DETAIL_TEMPLATE, "LogicalUnit", "", "add"),
                                        Response.Status.BAD_REQUEST);
        } else {
            validateParameters(disk, 2, "provisionedSize", "format"); // Non lun disks require size and format
        }
    }

    private Guid getStorageDomainIdByName(String storageDomainName) {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> storageDomains =
                getBackendCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                        QueryType.GetAllStorageDomains,
                        new QueryParametersBase());
        for (org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageName().equals(storageDomainName)) {
                return storageDomain.getId();
            }
        }
        return null;
    }

    @Override
    public Disks list() {
        //Even when filter=false the stored procedure GetAllDisksWithSnapshots is needed because of the
        //snapshot aggregation which it provides, so search alone is not enough. This is why in this case
        //the scenarios of filter=true and filter=false are not separated as they are in many other places
        return mapCollection(getBackendCollection(QueryType.GetAllDisksWithSnapshots, new QueryParametersBase(), SearchType.Disk));
    }

    @Override
    public DiskResource getDiskResource(String id) {
        return inject(new BackendDiskResource(id));
    }

    protected Disks mapCollection(List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities) {
        Disks collection = new Disks();
        for (org.ovirt.engine.core.common.businessentities.storage.Disk disk : entities) {
            collection.getDisks().add(addLinks(populate(map(disk), disk)));
        }
        return collection;
    }

    @Override
    protected Disk addLinks(Disk model, Class<? extends BaseResource> suggestedParent, String... subCollectionMembersToExclude) {
        // Currently the method that adds the links doesn't take into account that links need to be added also to
        // elements of lists, so whe need to add them explicitly:
        Disk disk = super.addLinks(model, suggestedParent, subCollectionMembersToExclude);
        if (disk.isSetStorageDomains()) {
            disk.getStorageDomains().getStorageDomains().forEach(
                storageDomain -> LinkHelper.addLinks(storageDomain, null, false)
            );
        }
        return disk;
    }
}
