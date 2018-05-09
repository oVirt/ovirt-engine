package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageServerConnectionResourceTest extends AbstractBackendSubResourceTest<StorageConnection, StorageServerConnections, BackendStorageServerConnectionResource> {
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
        return setUpEntityExpectations(mock(StorageServerConnections.class), index);
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        verifyModel(resource.get(), 3);
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetNotExistingEntityExpectations();
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testUpdate() {
        update(true, true, 2);
    }

    @Test
    public void testUpdateNotExistingConnection() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetNotExistingEntityExpectations();
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(3))));
    }

    @Test
    public void testUpdateCantDo() {
        verifyBadRequest(assertThrows(WebApplicationException.class, () -> update(false, false, 1)));
    }

    @Test
    public void testUpdateFailed() {
        verifyBadRequest(assertThrows(WebApplicationException.class, () -> update(true, false, 1)));
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations();
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId(GUIDS[3].toString());
        connection.setConnection("/data1");
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.RemoveStorageServerConnection,
            StorageServerConnectionParametersBase.class,
            new String[] { "StorageServerConnection", "VdsId" },
            new Object[] { connection, GUIDS[1] },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(
            uriInfo,
            BackendStorageServerConnectionResource.HOST,
            GUIDS[1].toString()
        );
        setUriInfo(uriInfo);
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNotExisting() {
        setUpGetNotExistingEntityExpectations();
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(
            uriInfo,
            BackendStorageServerConnectionResource.HOST,
            GUIDS[1].toString()
        );
        setUriInfo(uriInfo);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveValidateFail() {
        setUpGetEntityExpectations();
        Host host = new Host();
        host.setId(GUIDS[1].toString());
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId(GUIDS[3].toString());
        connection.setConnection("/data1");
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.RemoveStorageServerConnection,
            StorageServerConnectionParametersBase.class,
            new String[] { "StorageServerConnection", "VdsId" },
            new Object[] { connection, GUIDS[1] },
            false,
            false,
            false
        );
        uriInfo = addMatrixParameterExpectations(
                uriInfo,
                BackendStorageServerConnectionResource.HOST,
                GUIDS[1].toString()
        );
        setUriInfo(uriInfo);
        verifyBadRequest(assertThrows(WebApplicationException.class, () ->  resource.remove()));
    }

    protected void update(boolean valid, boolean executeCommandResult, int getConnectionExecTimes) {
        // the below method is called several times because
        // the mocked behavior must be recorded twice
        // since the getConnectionById query is executed
        // twice during a successful update operation.
        // not calling it twice causes the test to fail with NPE.
        for (int i = 0; i < getConnectionExecTimes; i++) {
            setUpGetEntityExpectations();
        }
        // not passing parameters to update action is OK because validate and execute results
        // are mocked anyway, and the real command is not called, so parameters are meaningless.
        setUriInfo(setUpActionExpectations(ActionType.UpdateStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] {},
                new Object[] {},
                valid,
                executeCommandResult));
        verifyModel(resource.update(getModel(3)), 3);
    }

    private void setUpGetEntityExpectations() {
        setUpEntityQueryExpectations(QueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[3].toString() },
                getEntity(3));
    }

    private void setUpGetNotExistingEntityExpectations() {
        setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[3].toString() },
                null);
    }

    static StorageServerConnections setUpEntityExpectations(StorageServerConnections entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index].toString());
        when(entity.getStorageType()).thenReturn(STORAGE_TYPES_MAPPED[index]);

        if (STORAGE_TYPES_MAPPED[index].equals(StorageType.ISCSI)) {
            when(entity.getPort()).thenReturn("3260");
            when(entity.getConnection()).thenReturn("1.122.10.125");
        }

        if (STORAGE_TYPES_MAPPED[index].equals(StorageType.NFS)) {
            when(entity.getConnection()).thenReturn("1.122.10.125:/data1");
        }

        return entity;
    }

    StorageConnection getModel(int index) {
        StorageConnection model = new StorageConnection();
        model.setType(STORAGE_TYPES[index]);
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
        assertEquals(STORAGE_TYPES_MAPPED[index].toString().toLowerCase(), model.getType().value());
        verifyLinks(model);
    }
}
