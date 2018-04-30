package org.ovirt.engine.api.restapi.resource;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendQuotaResourceTest extends AbstractBackendSubResourceTest<Quota, org.ovirt.engine.core.common.businessentities.Quota, BackendQuotaResource> {

    static final Guid QUOTA_ID = GUIDS[0];
    static final Guid DATACENTER_ID = GUIDS[1];

    public BackendQuotaResourceTest() {
        super(new BackendQuotaResource(QUOTA_ID.toString(), new BackendQuotasResource(DATACENTER_ID.toString())));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        Quota model = resource.get();
        verifyModel(model, 0);
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(ActionType.UpdateQuota,
                QuotaCRUDParameters.class,
                new String[] { "QuotaId", "Quota.Description", "Quota.GraceClusterPercentage" },
                new Object[] { QUOTA_ID, DESCRIPTIONS[0], 30 },
                true,
                true));

        verifyModel(resource.update(getModel()), 0);
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(
                ActionType.RemoveQuota,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { QUOTA_ID },
                true,
                true));
        verifyRemove(resource.remove());

    }

    private void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(QueryType.GetQuotaByQuotaId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { QUOTA_ID },
                getEntity(0));
    }

    private Quota getModel() {
        Quota quota = new Quota();
        quota.setId(GUIDS[0].toString());
        quota.setDescription(DESCRIPTIONS[0]);
        quota.setClusterHardLimitPct(30);
        return quota;
    }

    private void setUpGetEntityExpectations(int times) {
        while (times-- > 0) {
            setUpGetEntityExpectations();
        }

    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Quota getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Quota quota =
                new org.ovirt.engine.core.common.businessentities.Quota();
        quota.setId(GUIDS[index]);
        quota.setQuotaName(NAMES[index]);
        quota.setDescription(DESCRIPTIONS[index]);
        quota.setStoragePoolId(DATACENTER_ID);
        return quota;
    }
}
