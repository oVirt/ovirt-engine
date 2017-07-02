package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendAssignedDiskProfilesResourceTest extends AbstractBackendDiskProfilesResourceTest<BackendAssignedDiskProfilesResource> {

    public BackendAssignedDiskProfilesResourceTest() {
        super(new BackendAssignedDiskProfilesResource(STORAGE_DOMAIN_ID.toString()),
                QueryType.GetDiskProfilesByStorageDomainId,
                IdQueryParameters.class);
    }

    @Override
    protected List<DiskProfile> getCollection() {
        return collection.list().getDiskProfiles();
    }
}
