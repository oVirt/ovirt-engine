package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageServerConnectionsResourceTest extends AbstractBackendCollectionResourceTest<StorageConnection, StorageServerConnections, BackendStorageServerConnectionsResource> {

    protected static final org.ovirt.engine.api.model.StorageType[] STORAGE_TYPES = {
        org.ovirt.engine.api.model.StorageType.NFS,
        org.ovirt.engine.api.model.StorageType.LOCALFS,
        org.ovirt.engine.api.model.StorageType.POSIXFS,
        org.ovirt.engine.api.model.StorageType.ISCSI };

    protected static final StorageType[] STORAGE_TYPES_MAPPED = {
            StorageType.NFS,
            StorageType.LOCALFS,
            StorageType.POSIXFS,
            StorageType.ISCSI };

    public BackendStorageServerConnectionsResourceTest() {
        super(new BackendStorageServerConnectionsResource(), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Override
    protected List<StorageConnection> getCollection() {
        return collection.list().getStorageConnections();
    }

    @Override
    protected StorageServerConnections getEntity(int index) {
        return setUpEntityExpectations(control.createMock(StorageServerConnections.class), index);
    }

    StorageConnection getModel(int index) {
        StorageConnection model = new StorageConnection();
        model.setType(STORAGE_TYPES[index]);
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
        expect(entity.getId()).andReturn(GUIDS[index].toString()).anyTimes();
        expect(entity.getStorageType()).andReturn(STORAGE_TYPES_MAPPED[index]).anyTimes();
        expect(entity.getConnection()).andReturn("/data1").anyTimes();
        if (STORAGE_TYPES_MAPPED[index].equals(StorageType.ISCSI)) {
            expect(entity.getPort()).andReturn("3260").anyTimes();
        }

        return entity;
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetAllStorageServerConnections,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpStorageConnections(),
                failure);

        control.replay();
    }

    @Override
    protected void verifyCollection(List<StorageConnection> collection) throws Exception {
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

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setConnection("1.1.1.1:/data1");
        connection.setStorageType(STORAGE_TYPES_MAPPED[0]);
        setUpCreationExpectations(VdcActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection", "StorageServerConnection.StorageType", "VdsId" },
                new Object[] { connection.getConnection(), STORAGE_TYPES_MAPPED[0], GUIDS[1] },
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
        assertTrue(response.getEntity() instanceof StorageConnection);
        verifyModel((StorageConnection) response.getEntity(), 0);
    }

    @Test
    public void testAddLocal() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setConnection("/data1");
        connection.setStorageType(STORAGE_TYPES_MAPPED[1]);
        setUpCreationExpectations(VdcActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection", "StorageServerConnection.StorageType", "VdsId" },
                new Object[] { connection.getConnection(), STORAGE_TYPES_MAPPED[1], GUIDS[1] },
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
        assertTrue(response.getEntity() instanceof StorageConnection);
        verifyModel((StorageConnection) response.getEntity(), 1);
    }

    @Test
    public void testAddFailure() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setConnection("1.1.1.1:/data1");
        connection.setStorageType(STORAGE_TYPES_MAPPED[0]);
        setUpCreationExpectations(VdcActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection", "StorageServerConnection.StorageType", "VdsId" },
                new Object[] { connection.getConnection(), STORAGE_TYPES_MAPPED[0], GUIDS[1] },
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

    protected void verifyModel(StorageConnection model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(STORAGE_TYPES_MAPPED[index].toString().toLowerCase(), model.getType().value());
        verifyLinks(model);
    }

}
