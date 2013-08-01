package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.model.VnicProfiles;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendVnicProfilesResource
        extends AbstractBackendCollectionResource<VnicProfile, org.ovirt.engine.core.common.businessentities.network.VnicProfile> {

    public AbstractBackendVnicProfilesResource(String... subCollections) {
        super(VnicProfile.class,
                org.ovirt.engine.core.common.businessentities.network.VnicProfile.class,
                subCollections);
    }

    protected VnicProfiles mapCollection(List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> entities) {
        VnicProfiles collection = new VnicProfiles();
        for (org.ovirt.engine.core.common.businessentities.network.VnicProfile entity : entities) {
            collection.getVnicProfiles().add(addLinks(populate(map(entity), entity)));
        }

        return collection;
    }

    protected Response add(VnicProfile vnicProfile) {
        validateParameters(vnicProfile);
        return performCreate(VdcActionType.AddVnicProfile,
                new VnicProfileParameters(map(vnicProfile)),
                new QueryIdResolver<Guid>(VdcQueryType.GetVnicProfileById, IdQueryParameters.class));
    }

    protected abstract void validateParameters(VnicProfile vnicProfile);

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVnicProfile, new VnicProfileParameters(getVnicProfile(id)));
    }

    protected org.ovirt.engine.core.common.businessentities.network.VnicProfile getVnicProfile(String id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.network.VnicProfile.class,
                VdcQueryType.GetVnicProfileById,
                new IdQueryParameters(asGuidOr404(id)),
                "VnicProfiles");
    }

    @Override
    protected VnicProfile doPopulate(VnicProfile model,
            org.ovirt.engine.core.common.businessentities.network.VnicProfile entity) {
        return model;
    }

    protected abstract List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> getVnicProfilesCollection();

    protected VnicProfiles performList() {
        return mapCollection(getVnicProfilesCollection());
    }
}
