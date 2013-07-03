package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageServerConnectionsResourceTest extends AbstractBackendCollectionResourceTest<Storage, StorageServerConnections, BackendStorageServerConnectionsResource> {

    protected static final org.ovirt.engine.core.common.businessentities.StorageType STORAGE_TYPES_MAPPED[] = {
            org.ovirt.engine.core.common.businessentities.StorageType.NFS,
            org.ovirt.engine.core.common.businessentities.StorageType.LOCALFS,
            org.ovirt.engine.core.common.businessentities.StorageType.POSIXFS,
            org.ovirt.engine.core.common.businessentities.StorageType.ISCSI };

    public BackendStorageServerConnectionsResourceTest() {
        super(new BackendStorageServerConnectionsResource(), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Override
    protected List<Storage> getCollection() {
        return collection.list().getStorageConnections();
    }

    @Override
    protected StorageServerConnections getEntity(int index) {
        return setUpEntityExpectations(control.createMock(StorageServerConnections.class), index);
    }

    Storage getModel(int index) {
        Storage model = new Storage();
        model.setType(STORAGE_TYPES_MAPPED[index].toString());
        if ( index == 0 || index == 3 ) {
            model.setAddress("1.1.1.1");
        }
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        model.setHost(host);
        if (index == 0 || index == 1) {
            model.setPath("/data1");
        }
        return model;
    }

    static StorageServerConnections setUpEntityExpectations(StorageServerConnections entity, int index) {
        expect(entity.getid()).andReturn(GUIDS[index].toString()).anyTimes();
        expect(entity.getstorage_type()).andReturn(STORAGE_TYPES_MAPPED[index]).anyTimes();
        expect(entity.getconnection()).andReturn("/data1").anyTimes();
        if (STORAGE_TYPES_MAPPED[index].equals(StorageType.ISCSI)) {
            expect(entity.getport()).andReturn("3260").anyTimes();
        }

        return entity;
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert (query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetAllStorageServerConnections,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpStorageConnections(),
                failure);

        control.replay();
    }

    @Override
    protected void verifyCollection(List<Storage> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(GUIDS.length, collection.size());
    }

    protected List<StorageServerConnections> setUpStorageConnections() {
        List<StorageServerConnections> storageConnections = new ArrayList<>();
        for (int i = 0; i < GUIDS.length; i++) {
            storageConnections.add(getEntity(i));
        }
        return storageConnections;
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                getEntity(0));
    }

    private void setUpGetNotExistingEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                null);
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setid(GUIDS[0].toString());
        connection.setconnection("/data1");
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection", "VdsId" },
                new Object[] { connection, GUIDS[1] },
                true,
                true));
        verifyRemove(collection.remove(GUIDS[0].toString(), host));
    }

    @Test
    public void testRemoveNotExisting() throws Exception {
        setUpGetNotExistingEntityExpectations();
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        control.replay();
        try {
            collection.remove(GUIDS[0].toString(), host);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCanDoFail() throws Exception {
        setUpGetEntityExpectations();
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setid(GUIDS[0].toString());
        connection.setconnection("/data1");
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection", "VdsId" },
                new Object[] { connection, GUIDS[1] },
                false,
                false));
        try {
            collection.remove(GUIDS[0].toString(), host);
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(400, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setconnection("1.1.1.1:/data1");
        connection.setstorage_type(STORAGE_TYPES_MAPPED[0]);
        setUpCreationExpectations(VdcActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.connection", "StorageServerConnection.storage_type", "VdsId" },
                new Object[] { connection.getconnection(), STORAGE_TYPES_MAPPED[0], GUIDS[1] },
                true,
                true,
                GUIDS[0].toString(),
                VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                getEntity(0));

        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Storage);
        verifyModel((Storage) response.getEntity(), 0);
    }

    @Test
    public void testAddLocal() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setconnection("/data1");
        connection.setstorage_type(STORAGE_TYPES_MAPPED[1]);
        setUpCreationExpectations(VdcActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.connection", "StorageServerConnection.storage_type", "VdsId" },
                new Object[] { connection.getconnection(), STORAGE_TYPES_MAPPED[1], GUIDS[1] },
                true,
                true,
                GUIDS[1].toString(),
                VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[1].toString() },
                getEntity(1));

        Response response = collection.add(getModel(1));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Storage);
        verifyModel((Storage) response.getEntity(), 1);
    }

    @Test
    public void testAddFailure() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setconnection("1.1.1.1:/data1");
        connection.setstorage_type(STORAGE_TYPES_MAPPED[0]);
        setUpCreationExpectations(VdcActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.connection", "StorageServerConnection.storage_type", "VdsId" },
                new Object[] { connection.getconnection(), STORAGE_TYPES_MAPPED[0], GUIDS[1] },
                false,
                false,
                GUIDS[0].toString(),
                VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                getEntity(0));

        Response response = null;
        try {
            response = collection.add(getModel(0));
        } catch (WebApplicationException e) {
            assertNotNull(e.getResponse());
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    protected void verifyModel(Storage model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(STORAGE_TYPES_MAPPED[index].toString().toLowerCase(), model.getType());
        verifyLinks(model);
    }

}
