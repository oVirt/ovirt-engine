package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.core.common.action.MacPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendMacPoolsResourceTest
        extends AbstractBackendCollectionResourceTest<MacPool, org.ovirt.engine.core.common.businessentities.MacPool, BackendMacPoolsResource> {

    private VdcQueryType listQueryType;
    private Class<? extends VdcQueryParametersBase> listQueryParamsClass;

    public BackendMacPoolsResourceTest() {
        super(new BackendMacPoolsResource(), null, "");
        this.listQueryType = VdcQueryType.GetAllMacPools;
        this.listQueryParamsClass = VdcQueryParametersBase.class;
    }

    @Override
    protected List<MacPool> getCollection() {
        return collection.list().getMacPools();
    }

    @Test
    public void testAddMacPool() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddMacPool,
                MacPoolParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                VdcQueryType.GetMacPoolById,
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
    public void testAddMacPoolCantDo() throws Exception {
        doTestBadAddMacPool(false, true, CANT_DO);
    }

    @Test
    public void testAddMacPoolFailure() throws Exception {
        doTestBadAddMacPool(true, false, FAILURE);
    }

    private void doTestBadAddMacPool(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddMacPool,
                MacPoolParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));
        MacPool model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        MacPool model = createIncompleteMacPool();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "MacPool", "add", getIncompleteFields());
        }
    }

    protected String[] getIncompleteFields() {
        return new String[] { "name" };
    }

    protected MacPool createIncompleteMacPool() {
        MacPool macPool = new MacPool();
        return macPool;
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
        setUpMacPoolsQueryExpectations(null);
        control.replay();
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() throws Exception {
        setUpMacPoolsQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);
        control.replay();

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
        setUpMacPoolsQueryExpectations(t);

        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        control.replay();

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
        setUpMacPoolsQueryExpectations(t);
        collection.setUriInfo(uriInfo);

        control.replay();
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }

    private void setUpMacPoolsQueryExpectations(Object failure) {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        List<org.ovirt.engine.core.common.businessentities.MacPool> entities = new ArrayList<>();

        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            expect(queryResult.getReturnValue()).andReturn(entities).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String) failure);
            } else if (failure instanceof Exception) {
                expect(backend.runQuery(eq(listQueryType),
                        anyObject(listQueryParamsClass))).andThrow((Exception) failure).anyTimes();
                return;
            }
        }
        expect(backend.runQuery(eq(listQueryType), anyObject(listQueryParamsClass))).andReturn(
                queryResult);
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
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.MacPool.class),
                index);
    }

    static org.ovirt.engine.core.common.businessentities.MacPool setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.MacPool entity,
            int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        return entity;
    }
}
