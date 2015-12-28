package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotaResourceTest extends AbstractBackendSubResourceTest<Quota, org.ovirt.engine.core.common.businessentities.Quota, BackendQuotaResource> {

    static final Guid QUOTA_ID = GUIDS[0];
    static final Guid DATACENTER_ID = GUIDS[1];

    public BackendQuotaResourceTest() {
        super(new BackendQuotaResource(QUOTA_ID.toString(), new BackendQuotasResource(DATACENTER_ID.toString())));
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        control.replay();
        Quota model = resource.get();
        verifyModel(model, 0);
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateQuota,
                QuotaCRUDParameters.class,
                new String[] { "QuotaId", "Quota.Description", "Quota.GraceClusterPercentage" },
                new Object[] { QUOTA_ID, DESCRIPTIONS[1], 30 },
                true,
                true));

        verifyModel(resource.update(getModel()), 0);
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(
                VdcActionType.RemoveQuota,
                QuotaCRUDParameters.class,
                new String[] { "QuotaId" },
                new Object[] { QUOTA_ID },
                true,
                true));
        verifyRemove(resource.remove());

    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetQuotaByQuotaId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { QUOTA_ID },
                getEntity(0));
    }

    private Quota getModel() {
        Quota quota = new Quota();
        quota.setId(GUIDS[0].toString());
        quota.setDescription(DESCRIPTIONS[1]);
        quota.setClusterHardLimitPct(30);
        return quota;
    }

    private void setUpGetEntityExpectations(int times) throws Exception {
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
