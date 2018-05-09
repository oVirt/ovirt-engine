package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendNetworksResourceTest.getModel;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendDataCenterNetworkResourceTest
    extends AbstractBackendNetworkResourceTest<BackendDataCenterNetworkResource> {

    static Guid networkId = GUIDS[0];
    static Guid dataCenterId = GUIDS[1];

    public BackendDataCenterNetworkResourceTest() {
        super(new BackendDataCenterNetworkResource(GUIDS[0].toString(), new BackendDataCenterNetworksResource(dataCenterId.toString())));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(
                WebApplicationException.class, () -> new BackendDataCenterNetworkResource("foo", null)));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetNetworksByDataCenterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { dataCenterId },
                                     new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>());

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetNetworksByDataCenterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { dataCenterId },
                                     new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>());

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpEntityQueryExpectations(2);

        setUriInfo(setUpActionExpectations(ActionType.UpdateNetwork,
                                           AddNetworkStoragePoolParameters.class,
                                           new String[] { "StoragePoolId" },
                                           new Object[] { dataCenterId },
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(1);

        setUriInfo(setUpActionExpectations(ActionType.UpdateNetwork,
                                           AddNetworkStoragePoolParameters.class,
                                           new String[] { "StoragePoolId" },
                                           new Object[] { dataCenterId },
                                           valid,
                                           success));

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))), detail);
    }

    @Test
    public void testConflictedUpdate() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);

        Network model = getModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    @Test
    public void testRemoveNotFound() {
        setUpEntityQueryExpectations(
            QueryType.GetNetworksByDataCenterId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { dataCenterId },
            new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>()
        );

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemove() {
        setUpEntityQueryExpectations(2);
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveNetwork,
                RemoveNetworkParameters.class,
                new String[] { "Id" },
                new Object[] { networkId },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpEntityQueryExpectations(
            QueryType.GetNetworksByDataCenterId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { dataCenterId },
            new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>(),
            null
        );
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(2);
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveNetwork,
                RemoveNetworkParameters.class,
                new String[] { "Id" },
                new Object[] { networkId },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    protected void setUpEntityQueryExpectations(int times) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetNetworksByDataCenterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { dataCenterId },
                                         getEntityList());
        }
    }
}

