package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainServerConnectionResourceTest extends AbstractBackendSubResourceTest<StorageConnection, StorageServerConnections, BackendStorageDomainServerConnectionResource> {

    public BackendStorageDomainServerConnectionResourceTest() {
        super(new BackendStorageDomainServerConnectionResource(GUIDS[3].toString(), null));
        BackendStorageDomainServerConnectionsResource parent = new BackendStorageDomainServerConnectionsResource(GUIDS[2]);
        resource.parent = parent;
    }

    @Test
    public void testDetachSuccess() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpActionExpectations(ActionType.DetachStorageConnectionFromStorageDomain,
                AttachDetachStorageConnectionParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true);
        Response response = resource.remove();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDetachFailure() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpActionExpectations(ActionType.DetachStorageConnectionFromStorageDomain,
                AttachDetachStorageConnectionParameters.class,
                new String[] {},
                new Object[] {},
                false,
                false);
        verifyBadRequest(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Override
    protected StorageServerConnections getEntity(int index) {
        StorageServerConnections entity = new StorageServerConnections();
        entity.setId(GUIDS[index].toString());
        return entity;
    }

    private void setUpGetEntityExpectations() {
        setUpEntityQueryExpectations(QueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[3].toString() },
                getEntity(3));
    }
}
