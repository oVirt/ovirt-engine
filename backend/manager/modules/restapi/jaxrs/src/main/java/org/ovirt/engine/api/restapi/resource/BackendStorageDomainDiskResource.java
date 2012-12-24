package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.queries.GetDiskByDiskIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainDiskResource extends BackendDiskResource {

    final private String storageDomainId;;

    protected BackendStorageDomainDiskResource(String id, String storageDomainId) {
        super(id);
        this.storageDomainId = storageDomainId;
    }

    @Override
    protected Disk performGet(VdcQueryType query, VdcQueryParametersBase params) {
        Disk disk = super.performGet(VdcQueryType.GetDiskByDiskId, new GetDiskByDiskIdParameters(guid));
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
    protected Disk populate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        Disk populatedDisk = super.populate(model, entity);

        // this code generates back-link to the corresponding SD
        populatedDisk.setStorageDomain(new StorageDomain());
        populatedDisk.getStorageDomain().setId(this.storageDomainId);

        return model;
    }

    public String getStorageDomainId() {
        return storageDomainId;
    }
}
