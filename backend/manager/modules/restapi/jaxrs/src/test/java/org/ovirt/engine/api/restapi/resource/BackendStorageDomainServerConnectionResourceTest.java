package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainServerConnectionResourceTest extends AbstractBackendSubResourceTest<StorageConnection, StorageServerConnections, BackendStorageDomainServerConnectionResource> {

    public BackendStorageDomainServerConnectionResourceTest() {
        super(new BackendStorageDomainServerConnectionResource(GUIDS[3].toString(), null));
        BackendStorageDomainServerConnectionsResource parent = new BackendStorageDomainServerConnectionsResource(GUIDS[2]);
        resource.parent = parent;
    }

    @Test
    public void testDetachSuccess() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpActionExpectations(VdcActionType.DetachStorageConnectionFromStorageDomain,
                AttachDetachStorageConnectionParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true);
        Response response = resource.remove();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDetachFailure() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpActionExpectations(VdcActionType.DetachStorageConnectionFromStorageDomain,
                AttachDetachStorageConnectionParameters.class,
                new String[] {},
                new Object[] {},
                false,
                false);
        try {
            Response response = resource.remove();
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(400, wae.getResponse().getStatus());
        }
    }

    @Override
    protected StorageServerConnections getEntity(int index) {
        StorageServerConnections entity = new StorageServerConnections();
        entity.setId(GUIDS[index].toString());
        return entity;
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[3].toString() },
                getEntity(3));
    }
}
