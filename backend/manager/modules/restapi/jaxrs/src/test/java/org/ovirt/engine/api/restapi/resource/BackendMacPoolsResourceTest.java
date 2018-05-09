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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MacPoolParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendMacPoolsResourceTest
        extends AbstractBackendCollectionResourceTest<MacPool, org.ovirt.engine.core.common.businessentities.MacPool, BackendMacPoolsResource> {

    private QueryType listQueryType;

    public BackendMacPoolsResourceTest() {
        super(new BackendMacPoolsResource(), null, "");
        this.listQueryType = QueryType.GetAllMacPools;
    }

    @Override
    protected List<MacPool> getCollection() {
        return collection.list().getMacPools();
    }

    @Test
    public void testAddMacPool() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddMacPool,
                MacPoolParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                QueryType.GetMacPoolById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { Guid.Empty },
                getEntity(0));
        MacPool model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof MacPool);
        verifyModel((MacPool) response.getEntity(), 0);
    }

    @Test
    public void testAddMacPoolCantDo() {
        doTestBadAddMacPool(false, true, CANT_DO);
    }

    @Test
    public void testAddMacPoolFailure() {
        doTestBadAddMacPool(true, false, FAILURE);
    }

    private void doTestBadAddMacPool(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AddMacPool,
                MacPoolParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));
        MacPool model = getModel(0);

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        MacPool model = createIncompleteMacPool();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "MacPool", "add", getIncompleteFields());
    }

    protected String[] getIncompleteFields() {
        return new String[] { "name" };
    }

    protected MacPool createIncompleteMacPool() {
        MacPool macPool = new MacPool();
        return macPool;
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
        setUpMacPoolsQueryExpectations(null);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() {
        setUpMacPoolsQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Override
    @Test
    public void testListCrash() {
        Throwable t = new RuntimeException(FAILURE);
        setUpMacPoolsQueryExpectations(t);

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
        setUpMacPoolsQueryExpectations(t);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }

    private void setUpMacPoolsQueryExpectations(Object failure) {
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        List<org.ovirt.engine.core.common.businessentities.MacPool> entities = new ArrayList<>();

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

    static MacPool getModel(int index) {
        MacPool model = new MacPool();
        model.setId(GUIDS[index].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.MacPool getEntity(int index) {
        return setUpEntityExpectations(mock(org.ovirt.engine.core.common.businessentities.MacPool.class),
                index);
    }

    static org.ovirt.engine.core.common.businessentities.MacPool setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.MacPool entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        return entity;
    }
}
