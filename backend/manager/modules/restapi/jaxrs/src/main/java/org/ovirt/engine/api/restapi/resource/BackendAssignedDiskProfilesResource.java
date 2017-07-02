package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.DiskProfiles;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.AssignedDiskProfileResource;
import org.ovirt.engine.api.resource.AssignedDiskProfilesResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendAssignedDiskProfilesResource extends AbstractBackendDiskProfilesResource implements AssignedDiskProfilesResource {

    private final String storageDomainId;

    public BackendAssignedDiskProfilesResource(String storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    @Override
    public DiskProfiles list() {
        return performList();
    }

    @Override
    public Response add(DiskProfile diskProfile) {
        if (!diskProfile.isSetStorageDomain() || !diskProfile.getStorageDomain().isSetId()) {
            diskProfile.setStorageDomain(new StorageDomain());
            diskProfile.getStorageDomain().setId(storageDomainId);
        }

        return super.add(diskProfile);
    }

    @Override
    protected void validateParameters(DiskProfile diskProfile) {
        validateParameters(diskProfile, "name");
    }

    @Override
    public AssignedDiskProfileResource getProfileResource(String id) {
        return inject(new BackendAssignedDiskProfileResource(id, this));
    }

    @Override
    public DiskProfile addParents(DiskProfile diskProfile) {
        diskProfile.setStorageDomain(new StorageDomain());
        diskProfile.getStorageDomain().setId(storageDomainId);
        return diskProfile;
    }

    @Override
    protected List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> getDiskProfilesCollection() {
        return getBackendCollection(QueryType.GetDiskProfilesByStorageDomainId,
                new IdQueryParameters(asGuidOr404(storageDomainId)));
    }
}
