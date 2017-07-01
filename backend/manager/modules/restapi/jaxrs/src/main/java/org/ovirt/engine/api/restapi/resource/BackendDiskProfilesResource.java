package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.DiskProfiles;
import org.ovirt.engine.api.resource.DiskProfileResource;
import org.ovirt.engine.api.resource.DiskProfilesResource;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendDiskProfilesResource extends AbstractBackendDiskProfilesResource implements DiskProfilesResource {

    @Override
    public DiskProfiles list() {
        return performList();
    }

    @Override
    protected List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> getDiskProfilesCollection() {
        return getBackendCollection(QueryType.GetAllDiskProfiles, new QueryParametersBase());
    }

    @Override
    public Response add(DiskProfile diskProfile) {
        return super.add(diskProfile);
    }

    @Override
    protected void validateParameters(DiskProfile diskProfile) {
        validateParameters(diskProfile, "name", "storageDomain.id");
        String storageId = diskProfile.getStorageDomain().getId();
        // verify the storagedomain.id is well provided
        getEntity(StorageDomain.class,
                QueryType.GetStorageDomainById,
                new IdQueryParameters(asGuid(storageId)),
                "storagedomain: id="
                        + storageId);
    }

    @Override
    public DiskProfileResource getDiskProfileResource(String id) {
        return inject(new BackendDiskProfileResource(id));
    }
}
