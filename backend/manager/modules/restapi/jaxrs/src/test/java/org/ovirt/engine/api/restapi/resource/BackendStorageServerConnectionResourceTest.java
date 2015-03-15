package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageServerConnectionResourceTest extends AbstractBackendSubResourceTest<StorageConnection, StorageServerConnections, BackendStorageServerConnectionResource> {
    protected static final StorageType STORAGE_TYPES_MAPPED[] = {
            StorageType.NFS,
            StorageType.LOCALFS,
            StorageType.POSIXFS,
            StorageType.ISCSI };

    public BackendStorageServerConnectionResourceTest() {
        super(new BackendStorageServerConnectionResource(GUIDS[3].toString(),
                new BackendStorageServerConnectionsResource()));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected StorageServerConnections getEntity(int index) {
        return setUpEntityExpectations(control.createMock(StorageServerConnections.class), index);
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        control.replay();
        verifyModel(resource.get(), 3);
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetNotExistingEntityExpectations();
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        update(true, true, 2);
    }

    @Test
    public void testUpdateNotExistingConnection() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetNotExistingEntityExpectations();
        control.replay();
         try {
            resource.update(getModel(3));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }


    }

    @Test
    public void testUpdateCantDo() throws Exception {
        try {
            update(false, false, 1);
        } catch (WebApplicationException e) {
            assertNotNull(e.getResponse());
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void testUpdateFailed() throws Exception {
        try {
            update(true, false, 1);
        } catch (WebApplicationException e) {
            assertNotNull(e.getResponse());
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    protected void update(boolean canDo, boolean executeCommandResult, int getConnectionExecTimes) throws Exception {
        // the below method is called several times because
        // the mocked behavior must be recorded twice
        // since the getConnectionById query is executed
        // twice during a successful update operation.
        // not calling it twice causes the test to fail with NPE.
        for (int i = 0; i < getConnectionExecTimes; i++) {
            setUpGetEntityExpectations();
        }
        // not passing parameters to update action is OK because the canDo and execute results
        // are mocked anyway, and the real command is not called, so parameters are meaningless.
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] {},
                new Object[] {},
                canDo,
                executeCommandResult));
        verifyModel(resource.update(getModel(3)), 3);
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[3].toString() },
                getEntity(3));
    }

    private void setUpGetNotExistingEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[3].toString() },
                null);
    }

    static StorageServerConnections setUpEntityExpectations(StorageServerConnections entity, int index) {
        expect(entity.getid()).andReturn(GUIDS[index].toString()).anyTimes();
        expect(entity.getstorage_type()).andReturn(STORAGE_TYPES_MAPPED[index]).anyTimes();

        if (STORAGE_TYPES_MAPPED[index].equals(StorageType.ISCSI)) {
            expect(entity.getport()).andReturn("3260").anyTimes();
            expect(entity.getconnection()).andReturn("1.122.10.125").anyTimes();
        }

        if (STORAGE_TYPES_MAPPED[index].equals(StorageType.NFS)) {
            expect(entity.getconnection()).andReturn("1.122.10.125:/data1").anyTimes();
        }

        return entity;
    }

    StorageConnection getModel(int index) {
        StorageConnection model = new StorageConnection();
        model.setType(STORAGE_TYPES_MAPPED[index].toString());
        model.setAddress("1.122.10.125");
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        model.setHost(host);
        if (index == 0) {
            model.setPath("/data1");
        }
        if (index == 3) {
            model.setPort(3260);
        }
        return model;
    }

    protected void verifyModel(StorageConnection model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(STORAGE_TYPES_MAPPED[index].toString().toLowerCase(), model.getType());
        verifyLinks(model);
    }
}
