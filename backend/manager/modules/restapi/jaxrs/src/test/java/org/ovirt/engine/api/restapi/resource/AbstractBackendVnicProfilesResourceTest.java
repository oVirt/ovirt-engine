package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVnicProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendVnicProfilesResourceTest<C extends AbstractBackendVnicProfilesResource>
        extends AbstractBackendCollectionResourceTest<VnicProfile, org.ovirt.engine.core.common.businessentities.network.VnicProfile, C> {

    protected static final Guid NETWORK_ID = GUIDS[1];
    private QueryType listQueryType;

    public AbstractBackendVnicProfilesResourceTest(C collection,
            QueryType listQueryType,
            Class<? extends QueryParametersBase> queryParamsClass) {
        super(collection, null, "");
        this.listQueryType = listQueryType;
    }

    @Test
    public void testAddVnicProfile() {
        setUriInfo(setUpBasicUriExpectations());
        setUpNetworkQueryExpectations();
        setUpCreationExpectations(ActionType.AddVnicProfile,
                AddVnicProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                QueryType.GetVnicProfileById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { Guid.Empty },
                getEntity(0));
        VnicProfile model = getModel(0);
        model.setNetwork(new Network());
        model.getNetwork().setId(NETWORK_ID.toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VnicProfile);
        verifyModel((VnicProfile) response.getEntity(), 0);
    }

    @Test
    public void testAddVnicProfileCantDo() {
        setUpNetworkQueryExpectations();
        doTestBadAddVnicProfile(false, true, CANT_DO);
    }

    @Test
    public void testAddVnicProfileFailure() {
        setUpNetworkQueryExpectations();
        doTestBadAddVnicProfile(true, false, FAILURE);
    }

    private void doTestBadAddVnicProfile(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AddVnicProfile,
                AddVnicProfileParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));
        VnicProfile model = getModel(0);
        model.setNetwork(new Network());
        model.getNetwork().setId(NETWORK_ID.toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        VnicProfile model = createIncompleteVnicProfile();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "VnicProfile", "validateParameters", getIncompleteFields());
    }

    protected String[] getIncompleteFields() {
        return new String[] { "name" };
    }

    protected VnicProfile createIncompleteVnicProfile() {
        return new VnicProfile();
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpVnicProfilesQueryExpectations(null);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() {
        setUpVnicProfilesQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Override
    @Test
    public void testListCrash() {
        Throwable t = new RuntimeException(FAILURE);
        setUpVnicProfilesQueryExpectations(t);

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
        setUpVnicProfilesQueryExpectations(t);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }

    private void setUpVnicProfilesQueryExpectations(Object failure) {
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> entities = new ArrayList<>();

        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
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

    static VnicProfile getModel(int index) {
        VnicProfile model = new VnicProfile();
        model.setId(GUIDS[index].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.VnicProfile getEntity(int index) {
        return setUpEntityExpectations(mock(org.ovirt.engine.core.common.businessentities.network.VnicProfile.class),
                index);
    }

    protected void setUpNetworkQueryExpectations() {
    }

    static org.ovirt.engine.core.common.businessentities.network.VnicProfile setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.network.VnicProfile entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getNetworkId()).thenReturn(GUIDS[index]);
        return entity;
    }
}
