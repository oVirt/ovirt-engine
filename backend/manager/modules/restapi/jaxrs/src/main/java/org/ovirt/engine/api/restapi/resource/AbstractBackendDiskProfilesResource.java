package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.DiskProfiles;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendDiskProfilesResource
        extends AbstractBackendCollectionResource<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> {

    public AbstractBackendDiskProfilesResource(String... subCollections) {
        super(DiskProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class,
                subCollections);
    }

    protected DiskProfiles mapCollection(List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> entities) {
        DiskProfiles collection = new DiskProfiles();
        for (org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity : entities) {
            collection.getDiskProfiles().add(addLinks(populate(map(entity), entity)));
        }

        return collection;
    }

    protected Response add(DiskProfile diskProfile) {
        validateParameters(diskProfile);
        DiskProfileParameters parameters =
                new DiskProfileParameters();
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile map = map(diskProfile);
        parameters.setProfile(map);
        return performCreate(VdcActionType.AddDiskProfile,
                parameters,
                new QueryIdResolver<Guid>(VdcQueryType.GetDiskProfileById, IdQueryParameters.class));
    }

    protected abstract void validateParameters(DiskProfile diskProfile);

    @Override
    protected Response performRemove(String id) {
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile diskProfile = getDiskProfile(id);
        return performAction(VdcActionType.RemoveDiskProfile,
                new DiskProfileParameters(diskProfile,
                        diskProfile.getId()));
    }

    protected org.ovirt.engine.core.common.businessentities.profiles.DiskProfile getDiskProfile(String id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class,
                VdcQueryType.GetDiskProfileById,
                new IdQueryParameters(asGuidOr404(id)),
                "DiskProfiles");
    }

    @Override
    protected DiskProfile doPopulate(DiskProfile model,
            org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity) {
        return model;
    }

    protected abstract List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> getDiskProfilesCollection();

    protected DiskProfiles performList() {
        return mapCollection(getDiskProfilesCollection());
    }
}
