package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendDiskProfilesResourceTest extends AbstractBackendDiskProfilesResourceTest<BackendDiskProfilesResource> {

    public BackendDiskProfilesResourceTest() {
        super(new BackendDiskProfilesResource(), VdcQueryType.GetAllDiskProfiles, VdcQueryParametersBase.class);
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
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { STORAGE_DOMAIN_ID },
                new StorageDomain());
    }
}
