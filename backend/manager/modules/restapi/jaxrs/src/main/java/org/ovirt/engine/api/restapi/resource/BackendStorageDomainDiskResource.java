package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainDiskResource extends BackendDiskResource {

    final private String storageDomainId;
    final private BackendStorageDomainDisksResource parent;

    public static final String UNREGISTERED_CONSTRAINT_PARAMETER = "unregistered";

    protected BackendStorageDomainDiskResource(String id, BackendStorageDomainDisksResource parent) {
        super(id);
        this.storageDomainId = parent.getStorageDomainId().toString();
        this.parent = parent;
    }

    @Override
    protected Disk performGet(VdcQueryType query, VdcQueryParametersBase params) {
        Disk disk;
        if (QueryHelper.hasMatrixParam(getUriInfo(), UNREGISTERED_CONSTRAINT_PARAMETER)) {
            VdcQueryReturnValue result = runQuery(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(guid));
            if (!result.getSucceeded() || result.getReturnValue() == null) {
                Guid storageDomainGuid = asGuid(storageDomainId);
                disk = super.performGet(VdcQueryType.GetUnregisteredDisk, new GetUnregisteredDiskQueryParameters(guid, storageDomainGuid, parent.getStoragePoolIdForDomain(storageDomainGuid)));
            } else {
                // The disk was found in the first get which means it is already registered. We must return nothing since the unregistered
                // parameter was passed.
                return notFound();
            }
        } else {
            disk = super.performGet(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(guid));
        }
        if (disk.isSetStorageDomains() && !disk.getStorageDomains().getStorageDomains().isEmpty()) {
            for (StorageDomain sd : disk.getStorageDomains().getStorageDomains()) {
                if (sd.isSetId() && sd.getId().equals(this.storageDomainId)) {
                    return disk;
                }
            }
        }
        return notFound();
    }

    @Override
    protected Disk deprecatedPopulate(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        Disk populatedDisk = super.doPopulate(model, entity);

        // this code generates back-link to the corresponding SD
        populatedDisk.setStorageDomain(new StorageDomain());
        populatedDisk.getStorageDomain().setId(this.storageDomainId);

        return model;
    }

    public String getStorageDomainId() {
        return storageDomainId;
    }
}
