package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.NetworkAttachmentParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNetworkAttachmentsResourceTest<C extends AbstractBackendNetworkAttachmentsResource>
        extends AbstractBackendCollectionResourceTest<org.ovirt.engine.api.model.NetworkAttachment, NetworkAttachment, C> {

    private static final Guid NETWORK_ID = GUIDS[1];
    protected static final Guid HOST_ID = GUIDS[2];
    protected static final Guid HOST_NIC_ID = GUIDS[3];
    private QueryType listQueryType;

    public AbstractBackendNetworkAttachmentsResourceTest(C collection,
            QueryType listQueryType) {
        super(collection, null, "");
        this.listQueryType = listQueryType;
    }

    @Test
    public void testAddNetworkAttachment() {
        setUriInfo(setUpBasicUriExpectations());
        setUpVerifyHostExpectations();
        setUpCreationExpectations(ActionType.AddNetworkAttachment,
                NetworkAttachmentParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                QueryType.GetNetworkAttachmentById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { Guid.Empty },
                getEntity(0));

        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof org.ovirt.engine.api.model.NetworkAttachment);
        verifyModel((org.ovirt.engine.api.model.NetworkAttachment) response.getEntity(), 0);
    }

    @Test
    public void testAddNetworkAttachmentCantDo() {
        doTestBadAddNetworkAttachment(false, true, CANT_DO);
    }

    @Test
    public void testAddNetworkAttachmentFailure() {
        doTestBadAddNetworkAttachment(true, false, FAILURE);
    }

    @Test
    public void testAddIncompleteParameters() {
        org.ovirt.engine.api.model.NetworkAttachment model = createIncompleteNetworkAttachment();
        setUriInfo(setUpBasicUriExpectations());
        setUpVerifyHostExpectations();
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "NetworkAttachment", "add", getIncompleteFields());
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Override
    @Test
    public void testList() {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpNetworkAttachmentsQueryExpectations(null);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() {
        setUpNetworkAttachmentsQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Override
    @Test
    public void testListCrash() {
        Throwable t = new RuntimeException(FAILURE);
        setUpNetworkAttachmentsQueryExpectations(t);

        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_SERVER_LOCALE, t);
    }

    @Override
    @Test
    public void testListCrashClientLocale() {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpNetworkAttachmentsQueryExpectations(t);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }

    protected String[] getIncompleteFields() {
        return new String[] { "network.id|name" };
    }

    protected org.ovirt.engine.api.model.NetworkAttachment createIncompleteNetworkAttachment() {
        return new org.ovirt.engine.api.model.NetworkAttachment();
    }

    @Override
    protected List<org.ovirt.engine.api.model.NetworkAttachment> getCollection() {
        return collection.list().getNetworkAttachments();
    }

    @Override
    protected NetworkAttachment getEntity(int index) {
        return setUpEntityExpectations(mock(NetworkAttachment.class), index);
    }

    protected void verifyCollection(List<org.ovirt.engine.api.model.NetworkAttachment> collection) {
        assertNotNull(collection);
        assertEquals(GUIDS.length, collection.size());
        for (int i = 0; i < GUIDS.length; i++) {
            verifyModel(collection.get(i), i);
        }
    }

    @Override
    protected final void verifyModel(org.ovirt.engine.api.model.NetworkAttachment model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(GUIDS[index].toString(), model.getNetwork().getId());
        verifyModel(model);
        verifyLinks(model);
    }

    protected void verifyModel(org.ovirt.engine.api.model.NetworkAttachment model) {
    }

    protected org.ovirt.engine.api.model.NetworkAttachment getModel(int index) {
        org.ovirt.engine.api.model.NetworkAttachment model = new org.ovirt.engine.api.model.NetworkAttachment();
        model.setId(GUIDS[index].toString());
        model.setNetwork(new Network());
        model.getNetwork().setId(NETWORK_ID.toString());
        return model;
    }

    protected final NetworkAttachment setUpEntityExpectations(NetworkAttachment entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getNetworkId()).thenReturn(GUIDS[index]);
        setUpEntityExpectations(entity);
        return entity;
    }

    protected void setUpEntityExpectations(NetworkAttachment entity) {
    }

    private void doTestBadAddNetworkAttachment(boolean valid, boolean success, String detail) {
        setUpVerifyHostExpectations();
        setUriInfo(setUpActionExpectations(ActionType.AddNetworkAttachment,
                NetworkAttachmentParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(getModel(0))), detail);
    }

    private void setUpNetworkAttachmentsQueryExpectations(Object failure) {
        setUpVerifyHostExpectations();
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        List<NetworkAttachment> entities = new ArrayList<>();

        if (failure == null) {
            for (int i = 0; i < GUIDS.length; i++) {
                entities.add(getEntity(i));
            }
            queryResult.setReturnValue(entities);
        } else {
            if (failure instanceof String) {
                queryResult.setExceptionString((String) failure);
                setUpL10nExpectations((String) failure);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(listQueryType), any())).thenThrow((Exception) failure);
                return;
            }
        }
        when(backend.runQuery(eq(listQueryType), any())).thenReturn(queryResult);
    }

    /**
     * Prepares expectations so that any request to get a host by id will return a valid response, including a dummy
     * host object.
     */
    private void setUpVerifyHostExpectations() {
        QueryReturnValue result = new QueryReturnValue();
        VDS host = mock(VDS.class);
        result.setSucceeded(true);
        result.setReturnValue(host);
        when(backend.runQuery(eq(QueryType.GetVdsByVdsId), any())).thenReturn(result);

        QueryReturnValue interfacesByVdsIdResult = new QueryReturnValue();
        interfacesByVdsIdResult.setSucceeded(true);

        VdsNetworkInterface hostNic = new VdsNetworkInterface();
        hostNic.setId(HOST_NIC_ID);
        List<VdsNetworkInterface> hostNics = Collections.singletonList(hostNic);
        interfacesByVdsIdResult.setReturnValue(hostNics);
        when(backend.runQuery(eq(QueryType.GetVdsInterfacesByVdsId), any())).thenReturn(interfacesByVdsIdResult);
    }
}
