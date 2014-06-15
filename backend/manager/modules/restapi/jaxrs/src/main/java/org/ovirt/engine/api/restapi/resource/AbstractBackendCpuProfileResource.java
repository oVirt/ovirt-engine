package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public abstract class AbstractBackendCpuProfileResource
        extends AbstractBackendSubResource<CpuProfile, org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> {

    protected AbstractBackendCpuProfileResource(String id, String... subCollections) {
        super(id, CpuProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class,
                subCollections);
    }

    protected CpuProfile get() {
        return performGet(VdcQueryType.GetCpuProfileById, new IdQueryParameters(guid));
    }

    @Override
    protected CpuProfile doPopulate(CpuProfile model,
            org.ovirt.engine.core.common.businessentities.profiles.CpuProfile entity) {
        return model;
    }
}
