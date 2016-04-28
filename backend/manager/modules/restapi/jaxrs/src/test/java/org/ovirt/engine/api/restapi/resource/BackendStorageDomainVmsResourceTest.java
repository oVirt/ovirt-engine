package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainVmsResourceTest
    extends AbstractBackendCollectionResourceTest<Vm,
                                                  org.ovirt.engine.core.common.businessentities.VM,
                                                  BackendStorageDomainVmsResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    public BackendStorageDomainVmsResourceTest() {
        super(new BackendStorageDomainVmsResource(STORAGE_DOMAIN_ID), null, null);
    }

    @Test
    @Ignore
    public void testQuery() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testList() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testListFailure() throws Exception {

    }

    @Test
    @Override
    @Ignore
    public void testListCrash() throws Exception {

    }

    @Test
    @Override
    @Ignore
    public void testListCrashClientLocale() throws Exception {

    }

    @Test
    public void testListExport() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 1);
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, true);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpQueryExpectations(query, failure, StorageDomainType.Data, true);
    }

    protected void setUpQueryExpectations(String query, Object failure, StorageDomainType domainType, boolean replay) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));

        switch (domainType) {
        case Data:
            break;
        case ImportExport:
            setUpEntityQueryExpectations(VdcQueryType.GetVmsFromExportDomain,
                                         GetAllFromExportDomainQueryParameters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId"},
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID},
                                         setUpVms(),
                                         failure);
            break;
        default:
            break;
        }

        if (replay) {
            control.replay();
        }
    }

    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        return setUpEntityExpectations(vm, vm.getStatisticsData(), index);
    }

    protected List<org.ovirt.engine.core.common.businessentities.VM> setUpVms() {
        List<org.ovirt.engine.core.common.businessentities.VM> ret = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            ret.add(getEntity(i));
        }
        return ret;
    }

    public static org.ovirt.engine.core.common.businessentities.StorageDomain setUpStorageDomain(StorageDomainType domainType) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setId(STORAGE_DOMAIN_ID);
        entity.setStorageDomainType(domainType);
        return entity;
    }

    protected List<Vm> getCollection() {
        return collection.list().getVms();
    }

    protected void verifyModel(Vm model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model);
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id, int times) {
        while (times-->0) {
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { id },
                    setUpStoragePool());
        }
    }
}
