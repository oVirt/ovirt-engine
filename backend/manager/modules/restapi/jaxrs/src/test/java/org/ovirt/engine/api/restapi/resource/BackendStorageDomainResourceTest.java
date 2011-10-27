package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.setUpStorageServerConnection;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.verifyModelSpecific;

public class BackendStorageDomainResourceTest
        extends AbstractBackendSubResourceTest<StorageDomain, storage_domains, BackendStorageDomainResource> {

    public BackendStorageDomainResourceTest() {
        super(new BackendStorageDomainResource(GUIDS[0].toString(), new BackendStorageDomainsResource()));
    }

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
        setUpGetEntityExpectations(1, true);
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
        setUpGetEntityExpectations(1);
        setUpGetStorageServerConnectionExpectations(1);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
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
        setUpGetEntityExpectations(2);
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
        setUpGetEntityExpectations(1);
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
        setUpGetEntityExpectations(1);
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

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetStorageDomainById,
                                       StorageDomainQueryParametersBase.class,
                                       new String[] { "StorageDomainId" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : getEntity(0));
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
    protected storage_domains getEntity(int index) {
        return setUpEntityExpectations(control.createMock(storage_domains.class), index);
    }

    protected void verifyModel(StorageDomain model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }
}
