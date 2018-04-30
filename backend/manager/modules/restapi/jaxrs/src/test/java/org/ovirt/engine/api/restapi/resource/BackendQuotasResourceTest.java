package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddQuota,
                QuotaCRUDParameters.class,
                new String[] { "Quota.StoragePoolId", "Quota.ThresholdClusterPercentage",
                        "Quota.GraceClusterPercentage", "Quota.ThresholdStoragePercentage",
                        "Quota.GraceStoragePercentage" },
                new Object[] { DATACENTER_ID, CLUSTER_SOFT_LIMIT_PCT, CLUSTER_HARD_LIMIT_PCT, STORAGE_SOFT_LIMIT_PCT,
                        STORAGE_HARD_LIMIT_PCT },
                true,
                true,
                GUIDS[0],
                QueryType.GetQuotaByQuotaId,
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
        setUpEntityQueryExpectations(QueryType.GetQuotaByStoragePoolId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DATACENTER_ID },
                getQuotas());
    }

    @Test
    @Disabled
    @Override
    public void testListFailure() {}

    @Test
    @Disabled
    @Override
    public void testListCrash() {}

    @Test
    @Disabled
    @Override
    public void testListCrashClientLocale() {}

    @Test
    @Disabled
    @Override
    public void testQuery() {} //queries on quotas not supported by API yet.
}
