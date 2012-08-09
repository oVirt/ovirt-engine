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
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDiskByDiskIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDisksResource extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.Disk> implements DisksResource{

    private static final String SUB_COLLECTIONS = "statistics";
    public BackendDisksResource() {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class, SUB_COLLECTIONS);
    }

    @Override
    public Response add(Disk disk) {
        validateParameters(disk, "provisionedSize|size", "format", "interface");
        validateEnums(Disk.class, disk);
        AddDiskParameters params = new AddDiskParameters();
        params.setDiskInfo(getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class).map(disk, null));
        if (disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains() && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
            params.setStorageDomainId(Guid.createGuidFromString(disk.getStorageDomains().getStorageDomains().get(0).getId()));
        } else if (disk.isSetStorageDomains() && disk.getStorageDomains().getStorageDomains().get(0).isSetName()) {
            params.setStorageDomainId(
                    getEntity(storage_domains.class,
                            SearchType.StorageDomain,
                            "Storage: name=" + disk.getStorageDomains().getStorageDomains().get(0).getName()).getId());
        }
        return performCreation(VdcActionType.AddDisk, params,
                new QueryIdResolver(VdcQueryType.GetDiskByDiskId, GetDiskByDiskIdParameters.class));
    }

    @Override
    public Disks list() {
        return mapCollection(getBackendCollection(SearchType.Disk));
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
