package org.ovirt.engine.api.restapi.resource;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotasResourceTest
    extends AbstractBackendCollectionResourceTest<Quota, org.ovirt.engine.core.common.businessentities.Quota, BackendQuotasResource> {

    private static final int STORAGE_SOFT_LIMIT_PCT = 40;
    private static final int STORAGE_HARD_LIMIT_PCT = 30;
    private static final int CLUSTER_SOFT_LIMIT_PCT = 20;
    private static final int CLUSTER_HARD_LIMIT_PCT = 10;
    public static final Guid DATACENTER_ID = GUIDS[0];

    public BackendQuotasResourceTest() {
        super(new BackendQuotasResource(DATACENTER_ID.toString()), null, null);
    }

    @Override
    protected List<Quota> getCollection() {
        return collection.list().getQuotas();
    }

    protected List<org.ovirt.engine.core.common.businessentities.Quota> getQuotas() {
        List<org.ovirt.engine.core.common.businessentities.Quota> quotas = new LinkedList<>();
        for (int index=0; index<NAMES.length; index++) {
            quotas.add(getEntity(index));
        }
        return quotas;
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setGetQuotasExpectations();
        control.replay();
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddQuota,
                QuotaCRUDParameters.class,
                new String[] { "Quota.StoragePoolId", "Quota.ThresholdClusterPercentage",
                        "Quota.GraceClusterPercentage", "Quota.ThresholdStoragePercentage",
                        "Quota.GraceStoragePercentage" },
                new Object[] { DATACENTER_ID, CLUSTER_SOFT_LIMIT_PCT, CLUSTER_HARD_LIMIT_PCT, STORAGE_SOFT_LIMIT_PCT,
                        STORAGE_HARD_LIMIT_PCT },
                true,
                true,
                GUIDS[0],
                VdcQueryType.GetQuotaByQuotaId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
        Quota quota = getQuota();
        Response response = collection.add(quota);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Quota);
        verifyModel((Quota) response.getEntity(), 0);
    }

    private Quota getQuota() {
        Quota quota = new Quota();
        quota.setName("Quota_Name");
        quota.setClusterHardLimitPct(CLUSTER_HARD_LIMIT_PCT);
        quota.setClusterSoftLimitPct(CLUSTER_SOFT_LIMIT_PCT);
        quota.setStorageHardLimitPct(STORAGE_HARD_LIMIT_PCT);
        quota.setStorageSoftLimitPct(STORAGE_SOFT_LIMIT_PCT);
        return quota;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Quota getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Quota quota =
                new org.ovirt.engine.core.common.businessentities.Quota();
        quota.setId(GUIDS[index]);
        quota.setDescription(DESCRIPTIONS[index]);
        quota.setQuotaName(NAMES[index]);
        quota.setStoragePoolId(GUIDS[index]);
        return quota;
    }

    private void setGetQuotasExpectations() {
        setUpEntityQueryExpectations(VdcQueryType.GetQuotaByStoragePoolId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DATACENTER_ID },
                getQuotas());
    }

    @Test
    @Ignore
    @Override
    public void testListFailure() throws Exception {}

    @Test
    @Ignore
    @Override
    public void testListCrash() throws Exception {}

    @Test
    @Ignore
    @Override
    public void testListCrashClientLocale() throws Exception {}

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {} //queries on quotas not supported by API yet.
}
