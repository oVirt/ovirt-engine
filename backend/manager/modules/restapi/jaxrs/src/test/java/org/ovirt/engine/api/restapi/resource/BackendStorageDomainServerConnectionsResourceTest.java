package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainServerConnectionsResourceTest extends AbstractBackendCollectionResourceTest<StorageConnection, StorageServerConnections, BackendStorageDomainServerConnectionsResource> {
    protected static final StorageType[] STORAGE_TYPES_MAPPED = {
            StorageType.NFS,
            StorageType.LOCALFS,
            StorageType.POSIXFS,
            StorageType.ISCSI };

    public BackendStorageDomainServerConnectionsResourceTest() {
        super(new BackendStorageDomainServerConnectionsResource(GUIDS[3]), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testAttachSuccess() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpActionExpectations(VdcActionType.AttachStorageConnectionToStorageDomain,
                AttachDetachStorageConnectionParameters.class,
                new String[] { },
                new Object[] { },
                true,
                true);
        StorageConnection connection = new StorageConnection();
        connection.setId(GUIDS[3].toString());
        Response response = collection.add(connection);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testAttachFailure() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpActionExpectations(VdcActionType.AttachStorageConnectionToStorageDomain,
                AttachDetachStorageConnectionParameters.class,
                new String[] { },
                new Object[] { },
                false,
                false);
        StorageConnection connection = new StorageConnection();
        connection.setId(GUIDS[3].toString());
       try {
            Response response = collection.add(connection);
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(400, wae.getResponse().getStatus());
        }
    }

    @Override
    protected List<StorageConnection> getCollection() {
        return collection.list().getStorageConnections();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetStorageServerConnectionsForDomain,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpStorageConnections(),
                failure);

        control.replay();
    }

    protected List<StorageServerConnections> setUpStorageConnections() {
        List<StorageServerConnections> storageConnections = new ArrayList<>();
        storageConnections.add(getEntity(3));
        return storageConnections;
    }

    @Override
    protected void verifyCollection(List<StorageConnection> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(1, collection.size());
    }

    @Override
    protected StorageServerConnections getEntity(int index) {
        return setUpEntityExpectations(control.createMock(StorageServerConnections.class), index);
    }

    static StorageServerConnections setUpEntityExpectations(StorageServerConnections entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index].toString()).anyTimes();
        expect(entity.getStorageType()).andReturn(STORAGE_TYPES_MAPPED[index]).anyTimes();
        expect(entity.getConnection()).andReturn("1.1.1.255").anyTimes();
        if (STORAGE_TYPES_MAPPED[index].equals(StorageType.ISCSI)) {
            expect(entity.getPort()).andReturn("3260").anyTimes();
        }

        return entity;
    }
}
