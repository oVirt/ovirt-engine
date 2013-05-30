package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParamenters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;

import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.verifyModelSpecific;

public class BackendStorageDomainVmsResourceTest
    extends AbstractBackendCollectionResourceTest<VM,
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

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, false);
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        vm.setId(GUIDS[0]);
        String[] names = new String[]{"Vm", "StorageDomainId", "StoragePoolId"};
        Object[] values = new Object[]{vm, GUIDS[3], DATA_CENTER_ID};
        setUpActionExpectations(VdcActionType.RemoveVmFromImportExport, RemoveVmFromImportExportParamenters.class, names, values, true, true);
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpQueryExpectations(query, failure, StorageDomainType.Data, true);
    }

    protected void setUpQueryExpectations(String query, Object failure, StorageDomainType domainType, boolean replay) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     StorageDomainQueryParametersBase.class,
                                     new String[] { "StorageDomainId" },
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
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.VM.class),
                                       control.createMock(VmStatistics.class),
                                       index);
    }

    protected List<org.ovirt.engine.core.common.businessentities.VM> setUpVms() {
        List<org.ovirt.engine.core.common.businessentities.VM> ret =
            new ArrayList<org.ovirt.engine.core.common.businessentities.VM>();
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

    protected List<VM> getCollection() {
        return collection.list().getVMs();
    }

    protected void verifyModel(VM model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id, int times) {
        while (times-->0) {
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                    StorageDomainQueryParametersBase.class,
                    new String[] { "StorageDomainId" },
                    new Object[] { id },
                    setUpStoragePool());
        }
    }
}
