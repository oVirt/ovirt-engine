package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendCpuProfilesResourceTest<C extends AbstractBackendCpuProfilesResource>
        extends AbstractBackendCollectionResourceTest<CpuProfile, org.ovirt.engine.core.common.businessentities.profiles.CpuProfile, C> {

    protected static final Guid CLUSTER_ID = GUIDS[1];
    private final QueryType listQueryType;

    public AbstractBackendCpuProfilesResourceTest(C collection, QueryType listQueryType) {
        super(collection, null, "");
        this.listQueryType = listQueryType;
    }

    @Test
    public void testAddCpuProfile() {
        setUriInfo(setUpBasicUriExpectations());
        setUpClusterQueryExpectations();
        setUpCreationExpectations(ActionType.AddCpuProfile,
                CpuProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                QueryType.GetCpuProfileById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { Guid.Empty },
                getEntity(0));
        CpuProfile model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(CLUSTER_ID.toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof CpuProfile);
        verifyModel((CpuProfile) response.getEntity(), 0);
    }

    @Test
    public void testAddCpuProfileCantDo() {
        setUpClusterQueryExpectations();
        doTestBadAddCpuProfile(false, true, CANT_DO);
    }

    @Test
    public void testAddCpuProfileFailure() {
        setUpClusterQueryExpectations();
        doTestBadAddCpuProfile(true, false, FAILURE);
    }

    private void doTestBadAddCpuProfile(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AddCpuProfile,
                CpuProfileParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));
        CpuProfile model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(CLUSTER_ID.toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        CpuProfile model = createIncompleteCpuProfile();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "CpuProfile", "validateParameters", getIncompleteFields());
    }

    protected String[] getIncompleteFields() {
        return new String[] { "name" };
    }

    protected CpuProfile createIncompleteCpuProfile() {
        return new CpuProfile();
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
        setUpCpuProfilesQueryExpectations(null);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() {
        setUpCpuProfilesQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Override
    @Test
    public void testListCrash() {
        Throwable t = new RuntimeException(FAILURE);
        setUpCpuProfilesQueryExpectations(t);

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
        setUpCpuProfilesQueryExpectations(t);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }

    private void setUpCpuProfilesQueryExpectations(Object failure) {
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        List<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> entities = new ArrayList<>();

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

    static CpuProfile getModel(int index) {
        CpuProfile model = new CpuProfile();
        model.setId(GUIDS[index].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.profiles.CpuProfile getEntity(int index) {
        return setUpEntityExpectations(mock(org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class),
                index);
    }

    protected void setUpClusterQueryExpectations() {
    }

    static org.ovirt.engine.core.common.businessentities.profiles.CpuProfile setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.profiles.CpuProfile entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getClusterId()).thenReturn(GUIDS[index]);
        return entity;
    }
}
