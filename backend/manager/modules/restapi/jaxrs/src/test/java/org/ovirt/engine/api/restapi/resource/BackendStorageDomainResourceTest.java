package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.setUpStorageServerConnection;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainResourceTest
        extends AbstractBackendSubResourceTest<StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain, BackendStorageDomainResource> {

    public BackendStorageDomainResourceTest() {
        super(new BackendStorageDomainResource(GUIDS[0].toString(), new BackendStorageDomainsResource()));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendStorageDomainResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true, getEntity(0));
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUpGetEntityExpectations(1, getEntity(0));
        setUpGetStorageServerConnectionExpectations(1);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetFcp() throws Exception {
        setUpGetEntityExpectations(1, getFcpEntity());
        setUpGetEntityExpectations(VdcQueryType.GetLunsByVgId,
                GetLunsByVgIdParameters.class,
                new String[] { "VgId" },
                new Object[] { GUIDS[0].toString() },
                setUpLuns());
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        verifyGetFcp(resource.get());
    }

    private void verifyGetFcp(StorageDomain model) {
        assertEquals(GUIDS[0].toString(), model.getId());
        assertEquals(NAMES[0], model.getName());
        assertEquals(StorageDomainType.DATA.value(), model.getType());
        assertNotNull(model.getStorage());
        assertEquals(StorageType.FCP.value(), model.getStorage().getType());
        assertNotNull(model.getLinks().get(0).getHref());
    }

    protected List<LUNs> setUpLuns() {
        LUNs lun = new LUNs();
        lun.setLUN_id(GUIDS[2].toString());
        List<LUNs> luns = new ArrayList<LUNs>();
        luns.add(lun);
        return luns;
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getFcpEntity() {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        expect(entity.getId()).andReturn(GUIDS[0]).anyTimes();
        expect(entity.getStorageName()).andReturn(NAMES[0]).anyTimes();
        expect(entity.getStorageDomainType()).andReturn(org.ovirt.engine.core.common.businessentities.StorageDomainType.Data).anyTimes();
        expect(entity.getStorageType()).andReturn(org.ovirt.engine.core.common.businessentities.storage.StorageType.FCP).anyTimes();
        expect(entity.getStorage()).andReturn(GUIDS[0].toString()).anyTimes();
        return entity;
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true, getEntity(0));
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2, getEntity(0));
        setUpGetStorageServerConnectionExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateStorageDomain,
                                           StorageDomainManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1, getEntity(0));
        setUpGetStorageServerConnectionExpectations(1);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateStorageDomain,
                                           StorageDomainManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUpGetEntityExpectations(1, getEntity(0));
        setUpGetStorageServerConnectionExpectations(1);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        StorageDomain model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected void setUpGetEntityExpectations(int times, org.ovirt.engine.core.common.businessentities.StorageDomain entity) throws Exception {
        setUpGetEntityExpectations(times, false, entity);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound, org.ovirt.engine.core.common.businessentities.StorageDomain entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetStorageDomainById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    protected void setUpGetStorageServerConnectionExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                                       StorageServerConnectionQueryParametersBase.class,
                                       new String[] { "ServerConnectionId" },
                                       new Object[] { GUIDS[0].toString() },
                                       setUpStorageServerConnection(0));
        }
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomain.class), index);
    }

    @Override
    protected void verifyModel(StorageDomain model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }
}
