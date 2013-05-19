package org.ovirt.engine.api.restapi.resource;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotasResourceTest
    extends AbstractBackendCollectionResourceTest<Quota, org.ovirt.engine.core.common.businessentities.Quota, BackendQuotasResource> {

    public static final Guid PARENT_GUID = GUIDS[0];

    public BackendQuotasResourceTest() {
        super(new BackendQuotasResource(GUIDS[0].toString()), null, null);
    }

    @Override
    protected List<Quota> getCollection() {
        return collection.list().getQuotas();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Quota getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Quota quota = new org.ovirt.engine.core.common.businessentities.Quota();
        quota.setId(GUIDS[index]);
        quota.setDescription(DESCRIPTIONS[index]);
        quota.setQuotaName(NAMES[index]);
        quota.setStoragePoolId(GUIDS[index]);
        return quota;
    }

    protected List<org.ovirt.engine.core.common.businessentities.Quota> getQuotas() {
        List<org.ovirt.engine.core.common.businessentities.Quota> quotas = new LinkedList<org.ovirt.engine.core.common.businessentities.Quota>();
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

    private void setGetQuotasExpectations() {
        setUpEntityQueryExpectations(VdcQueryType.GetQuotaByStoragePoolId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { PARENT_GUID },
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
