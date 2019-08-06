package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.resource.AttachedStorageDomainDiskResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredEntityQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAttachedStorageDomainDiskResource
        extends AbstractBackendStorageDomainDiskResource
        implements AttachedStorageDomainDiskResource {

    public BackendAttachedStorageDomainDiskResource(Guid storageDomainId, String diskId) {
        super(storageDomainId, diskId);
    }

    @Override
    public Response register(Action action) {
        // First we need to query the backend to fill in all the information about the disk from the VDSM.
        // We don't just use the information from the Disk object because it's missing a few things like creation
        // date and last modified date.
        Guid dataCenterId = BackendDataCenterHelper.lookupByStorageDomainId(this, storageDomainId);
        GetUnregisteredDiskQueryParameters getDiskParams =
                new GetUnregisteredDiskQueryParameters(guid, storageDomainId, dataCenterId);
        DiskImage unregisteredDisk =
                getEntity(DiskImage.class, QueryType.GetUnregisteredDisk, getDiskParams, guid.toString());
        RegisterDiskParameters registerDiskParams = new RegisterDiskParameters(unregisteredDisk, storageDomainId);
        return doAction(ActionType.RegisterDisk, registerDiskParams, action);
    }

    @Override
    public Response remove() {
        if (isUnregisteredDisk()) {
            RemoveDiskParameters removeDiskParameters = new RemoveDiskParameters(guid, storageDomainId);
            removeDiskParameters.setUnregisteredDisk(true);
            return performAction(ActionType.RemoveDisk, removeDiskParameters);
        }
        return super.remove();
    }

    private boolean isUnregisteredDisk() {
        UnregisteredDisk unregisteredDisk;
        try {
            GetUnregisteredEntityQueryParameters unregisteredDiskQueryParameters =
                    new GetUnregisteredEntityQueryParameters(storageDomainId, guid);
            unregisteredDisk =
                    getEntity(UnregisteredDisk.class,
                            QueryType.GetUnregisteredDiskFromDB,
                            unregisteredDiskQueryParameters,
                            guid.toString());
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return false;
            }
            throw e;
        }

        return unregisteredDisk != null;
    }
}
