package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    @Disabled
    public void testQuery() {
    }

    @Test
    @Override
    @Disabled
    public void testList() throws Exception {
    }

    @Test
    @Override
    @Disabled
    public void testListFailure() {

    }

    @Test
    @Override
    @Disabled
    public void testListCrash() {

    }

    @Test
    @Override
    @Disabled
    public void testListCrashClientLocale() {

    }

    @Test
    public void testListExport() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 1);
        setUpQueryExpectations("", null, StorageDomainType.ImportExport);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpQueryExpectations(query, failure, StorageDomainType.Data);
    }

    protected void setUpQueryExpectations(String query, Object failure, StorageDomainType domainType) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));

        switch (domainType) {
        case Data:
            break;
        case ImportExport:
            setUpEntityQueryExpectations(QueryType.GetVmsFromExportDomain,
                                         GetAllFromExportDomainQueryParameters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId"},
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID},
                                         setUpVms(),
                                         failure);
            break;
        default:
            break;
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
            setUpEntityQueryExpectations(QueryType.GetStoragePoolsByStorageDomainId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { id },
                    setUpStoragePool());
        }
    }
}
