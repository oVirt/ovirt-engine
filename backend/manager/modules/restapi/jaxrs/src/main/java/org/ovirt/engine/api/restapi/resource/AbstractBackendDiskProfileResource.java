package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public abstract class AbstractBackendDiskProfileResource
        extends AbstractBackendSubResource<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> {

    protected AbstractBackendDiskProfileResource(String id, String... subCollections) {
        super(id, DiskProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class,
                subCollections);
    }

    protected DiskProfile get() {
        return performGet(VdcQueryType.GetDiskProfileById, new IdQueryParameters(guid));
    }

    @Override
    protected DiskProfile doPopulate(DiskProfile model,
            org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity) {
        return model;
    }
}
