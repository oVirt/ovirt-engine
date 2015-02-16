package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendReadOnlyDisksResource
        extends AbstractBackendReadOnlyDevicesResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.Disk>
        implements ReadOnlyDevicesResource<Disk, Disks> {

    public BackendReadOnlyDisksResource(Guid parentId, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        super(Disk.class, Disks.class, org.ovirt.engine.core.common.businessentities.Disk.class, parentId, queryType, queryParams);
    }

    @Override
    public Disks list() {
        IdQueryParameters queryParams = new IdQueryParameters(parentId);
        List<org.ovirt.engine.core.common.businessentities.Disk> backendCollection = getBackendCollection(queryType, queryParams);
        Disks disks = mapCollection(backendCollection, false);
        for (Disk disk : disks.getDisks()) {
            disk.setVm(null);
            addLinks(disk);
        }
        return disks;
    }

    @Override
    protected <T> boolean matchEntity(org.ovirt.engine.core.common.businessentities.Disk entity, T id) {
        return id != null && id.equals(entity.getId());
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        return model;
    }
}
