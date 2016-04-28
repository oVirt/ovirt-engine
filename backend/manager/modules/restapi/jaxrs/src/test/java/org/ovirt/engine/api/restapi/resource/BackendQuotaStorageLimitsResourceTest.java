package org.ovirt.engine.api.restapi.resource;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.model.QuotaStorageLimits;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotaStorageLimitsResourceTest extends AbstractBackendBaseTest {

    private static final double STORAGE_SIZE_GB_USAGE = 20.0;
    private static final Long STORAGE_SIZE_GB = 10L;
    protected static final Guid QUOTA_ID = GUIDS[0];
    protected static final Guid DATACENTER_ID = GUIDS[1];
    protected static final Guid STORAGE_ID_1 = GUIDS[2];
    protected static final Guid STORAGE_ID_2 = GUIDS[3];

    protected BackendQuotaStorageLimitsResource collection;

    @Test
    public void testListGlobalLimit() throws Exception {
        Quota quota = getQuota();
        quota.setGlobalQuotaStorage(getStorageGlobalCpuLimit());
        setUpGetEntityExpectations(quota);
        control.replay();
        QuotaStorageLimits storageLimits = collection.list();
        assertStorageLimitsFound(storageLimits, 1);
        QuotaStorageLimit storageLimit = storageLimits.getQuotaStorageLimits().get(0);
        assertEquals(STORAGE_SIZE_GB, storageLimit.getLimit());
    }

    @Test
    public void testListNonGlobalLimit() throws Exception {
        Quota quota = getQuota();
        List<QuotaStorage> storageLimits = new LinkedList<>();
        QuotaStorage storageLimit1 = new QuotaStorage();
        storageLimit1.setStorageSizeGB(STORAGE_SIZE_GB);
        storageLimit1.setStorageId(STORAGE_ID_1);
        QuotaStorage storageLimit2 = new QuotaStorage();
        storageLimit2.setStorageSizeGBUsage(STORAGE_SIZE_GB_USAGE);
        storageLimit2.setStorageId(STORAGE_ID_2);
        storageLimits.add(storageLimit1);
        storageLimits.add(storageLimit2);
        quota.setQuotaStorages(storageLimits);
        setUpGetEntityExpectations(quota);
        control.replay();
        QuotaStorageLimits list = collection.list();
        assertStorageLimitsFound(list, 2);
        for (QuotaStorageLimit storageLimit : list.getQuotaStorageLimits()) {
            if (storageLimit.getStorageDomain().getId().equals(STORAGE_ID_1.toString())) {
                assertEquals(STORAGE_SIZE_GB, storageLimit.getLimit());
            }
            if (storageLimit.getStorageDomain().getId().equals(STORAGE_ID_2.toString())) {
                assertTrue(storageLimit.getUsage() == STORAGE_SIZE_GB_USAGE);
            }
        }

    }

    private void assertStorageLimitsFound(QuotaStorageLimits storageLimits, int resultsNum) {
        assertNotNull(storageLimits);
        assertNotNull(storageLimits.getQuotaStorageLimits());
        assertEquals(storageLimits.getQuotaStorageLimits().size(), resultsNum);
    }

    private QuotaStorage getStorageGlobalCpuLimit() {
        QuotaStorage storageGlobalLimit = new QuotaStorage();
        storageGlobalLimit.setQuotaId(GUIDS[0]);
        storageGlobalLimit.setStorageId(STORAGE_ID_1);
        storageGlobalLimit.setStorageSizeGB(STORAGE_SIZE_GB);
        return storageGlobalLimit;
    }

    protected org.ovirt.engine.core.common.businessentities.Quota getQuota() {
        org.ovirt.engine.core.common.businessentities.Quota quota =
                new org.ovirt.engine.core.common.businessentities.Quota();
        quota.setId(QUOTA_ID);
        quota.setStoragePoolId(DATACENTER_ID);
        return quota;
    }

    private void setUpGetEntityExpectations(Quota quota) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetQuotaByQuotaId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { QUOTA_ID },
                quota);
    }

    @Override
    protected void init() {
        collection = new BackendQuotaStorageLimitsResource(QUOTA_ID);
        collection.setMappingLocator(mapperLocator);
        initBackendResource(collection);
    }

}
