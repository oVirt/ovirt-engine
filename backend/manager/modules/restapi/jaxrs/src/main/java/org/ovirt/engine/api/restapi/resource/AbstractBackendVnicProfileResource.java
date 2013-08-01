package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public abstract class AbstractBackendVnicProfileResource
        extends AbstractBackendSubResource<VnicProfile, org.ovirt.engine.core.common.businessentities.network.VnicProfile> {

    protected AbstractBackendVnicProfileResource(String id, String... subCollections) {
        super(id, VnicProfile.class,
                org.ovirt.engine.core.common.businessentities.network.VnicProfile.class,
                subCollections);
    }

    protected VnicProfile get() {
        return performGet(VdcQueryType.GetVnicProfileById, new IdQueryParameters(guid));
    }

    @Override
    protected VnicProfile doPopulate(VnicProfile model,
            org.ovirt.engine.core.common.businessentities.network.VnicProfile entity) {
        return model;
    }
}
