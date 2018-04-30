package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAssignedDiskProfilesResourceTest extends AbstractBackendDiskProfilesResourceTest<BackendAssignedDiskProfilesResource> {

    public BackendAssignedDiskProfilesResourceTest() {
        super(new BackendAssignedDiskProfilesResource(STORAGE_DOMAIN_ID.toString()), QueryType.GetDiskProfilesByStorageDomainId);
    }

    @Override
    protected List<DiskProfile> getCollection() {
        return collection.list().getDiskProfiles();
    }
}
