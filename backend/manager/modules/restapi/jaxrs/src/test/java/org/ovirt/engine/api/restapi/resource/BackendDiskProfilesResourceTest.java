package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendDiskProfilesResourceTest extends AbstractBackendDiskProfilesResourceTest<BackendDiskProfilesResource> {

    public BackendDiskProfilesResourceTest() {
        super(new BackendDiskProfilesResource(), QueryType.GetAllDiskProfiles);
    }

    @Override
    protected String[] getIncompleteFields() {
        return new String[] { "storageDomain.id" };
    }

    @Override
    protected DiskProfile createIncompleteDiskProfile() {
        DiskProfile diskProfile = super.createIncompleteDiskProfile();
        diskProfile.setName(NAMES[0]);
        return diskProfile;
    }

    @Override
    protected List<DiskProfile> getCollection() {
        return collection.list().getDiskProfiles();
    }

    @Override
    protected void setUpStorageDomainQueryExpectations() {
        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { STORAGE_DOMAIN_ID },
                new StorageDomain());
    }
}
