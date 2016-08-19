package org.ovirt.engine.api.restapi.resource;

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

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.NetworkAttachmentParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNetworkAttachmentsResourceTest<C extends AbstractBackendNetworkAttachmentsResource>
        extends AbstractBackendCollectionResourceTest<org.ovirt.engine.api.model.NetworkAttachment, NetworkAttachment, C> {

    private static final Guid NETWORK_ID = GUIDS[1];
    protected static final Guid HOST_ID = GUIDS[2];
    protected static final Guid HOST_NIC_ID = GUIDS[3];
    private VdcQueryType listQueryType;

    public AbstractBackendNetworkAttachmentsResourceTest(C collection,
            VdcQueryType listQueryType) {
        super(collection, null, "");
        this.listQueryType = listQueryType;
    }

    @Test
    public void testAddNetworkAttachment() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpVerifyHostExpectations();
        setUpCreationExpectations(VdcActionType.AddNetworkAttachment,
                NetworkAttachmentParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                VdcQueryType.GetNetworkAttachmentById,
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
    public void testAddNetworkAttachmentCantDo() throws Exception {
        doTestBadAddNetworkAttachment(false, true, CANT_DO);
    }

    @Test
    public void testAddNetworkAttachmentFailure() throws Exception {
        doTestBadAddNetworkAttachment(true, false, FAILURE);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        org.ovirt.engine.api.model.NetworkAttachment model = createIncompleteNetworkAttachment();
        setUriInfo(setUpBasicUriExpectations());
        setUpVerifyHostExpectations();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "NetworkAttachment", "add", getIncompleteFields());
        }
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpNetworkAttachmentsQueryExpectations(null);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() throws Exception {
        setUpNetworkAttachmentsQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    @Override
    @Test
    public void testListCrash() throws Exception {
        Throwable t = new RuntimeException(FAILURE);
        setUpNetworkAttachmentsQueryExpectations(t);

        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    @Override
    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpNetworkAttachmentsQueryExpectations(t);
        collection.setUriInfo(uriInfo);

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
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

    protected void verifyCollection(List<org.ovirt.engine.api.model.NetworkAttachment> collection) throws Exception {
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

    private void doTestBadAddNetworkAttachment(boolean valid, boolean success, String detail) throws Exception {
        setUpVerifyHostExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.AddNetworkAttachment,
                NetworkAttachmentParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));

        try {
            collection.add(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    private void setUpNetworkAttachmentsQueryExpectations(Object failure) {
        setUpVerifyHostExpectations();
        VdcQueryReturnValue queryResult = mock(VdcQueryReturnValue.class);
        when(queryResult.getSucceeded()).thenReturn(failure == null);
        List<NetworkAttachment> entities = new ArrayList<>();

        if (failure == null) {
            for (int i = 0; i < GUIDS.length; i++) {
                entities.add(getEntity(i));
            }
            when(queryResult.getReturnValue()).thenReturn(entities);
        } else {
            if (failure instanceof String) {
                when(queryResult.getExceptionString()).thenReturn((String) failure);
                setUpL10nExpectations((String) failure);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(listQueryType),
                        any(IdQueryParameters.class))).thenThrow((Exception) failure);
                return;
            }
        }
        when(backend.runQuery(eq(listQueryType), any(IdQueryParameters.class))).thenReturn(queryResult);
    }

    /**
     * Prepares expectations so that any request to get a host by id will return a valid response, including a dummy
     * host object.
     */
    private void setUpVerifyHostExpectations() {
        VdcQueryReturnValue result = mock(VdcQueryReturnValue.class);
        VDS host = mock(VDS.class);
        when(result.getSucceeded()).thenReturn(true);
        when(result.getReturnValue()).thenReturn(host);
        when(backend.runQuery(eq(VdcQueryType.GetVdsByVdsId), any(IdQueryParameters.class))).thenReturn(result);

        VdcQueryReturnValue interfacesByVdsIdResult = mock(VdcQueryReturnValue.class);
        when(interfacesByVdsIdResult.getSucceeded()).thenReturn(true);

        VdsNetworkInterface hostNic = new VdsNetworkInterface();
        hostNic.setId(HOST_NIC_ID);
        List<VdsNetworkInterface> hostNics = Collections.singletonList(hostNic);
        when(interfacesByVdsIdResult.getReturnValue()).thenReturn(hostNics);
        when(backend.runQuery(eq(VdcQueryType.GetVdsInterfacesByVdsId), any(IdQueryParameters.class)))
                .thenReturn(interfacesByVdsIdResult);
    }
}
