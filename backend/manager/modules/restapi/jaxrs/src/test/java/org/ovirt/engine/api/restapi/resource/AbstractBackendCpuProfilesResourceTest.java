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
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendCpuProfilesResourceTest<C extends AbstractBackendCpuProfilesResource>
        extends AbstractBackendCollectionResourceTest<CpuProfile, org.ovirt.engine.core.common.businessentities.profiles.CpuProfile, C> {

    protected static final Guid CLUSTER_ID = GUIDS[1];
    private final VdcQueryType listQueryType;
    private final Class<? extends VdcQueryParametersBase> listQueryParamsClass;

    public AbstractBackendCpuProfilesResourceTest(C collection,
            VdcQueryType listQueryType,
            Class<? extends VdcQueryParametersBase> queryParamsClass) {
        super(collection, null, "");
        this.listQueryType = listQueryType;
        this.listQueryParamsClass = queryParamsClass;
    }

    @Test
    public void testRemoveNotFound() throws Exception {
        setUpEntityQueryExpectations(1, 0, true);
        control.replay();
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(2, 0, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveCpuProfile,
                CpuProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetCpuProfileById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { NON_EXISTANT_GUID },
                null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(2, 0, false);

        setUriInfo(setUpActionExpectations(VdcActionType.RemoveCpuProfile,
                CpuProfileParameters.class,
                new String[] {},
                new Object[] {},
                canDo,
                success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddCpuProfile() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpClusterQueryExpectations();
        setUpCreationExpectations(VdcActionType.AddCpuProfile,
                CpuProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                VdcQueryType.GetCpuProfileById,
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
    public void testAddCpuProfileCantDo() throws Exception {
        setUpClusterQueryExpectations();
        doTestBadAddCpuProfile(false, true, CANT_DO);
    }

    @Test
    public void testAddCpuProfileFailure() throws Exception {
        setUpClusterQueryExpectations();
        doTestBadAddCpuProfile(true, false, FAILURE);
    }

    private void doTestBadAddCpuProfile(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddCpuProfile,
                CpuProfileParameters.class,
                new String[] {},
                new Object[] {},
                canDo,
                success));
        CpuProfile model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(CLUSTER_ID.toString());

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        CpuProfile model = createIncompleteCpuProfile();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "CpuProfile", "validateParameters", getIncompleteFields());
        }
    }

    protected String[] getIncompleteFields() {
        return new String[] { "name" };
    }

    protected CpuProfile createIncompleteCpuProfile() {
        return new CpuProfile();
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
        setUpCpuProfilesQueryExpectations(null);
        control.replay();
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() throws Exception {
        setUpCpuProfilesQueryExpectations(FAILURE);
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
        setUpCpuProfilesQueryExpectations(t);

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
        setUpCpuProfilesQueryExpectations(t);
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

    private void setUpCpuProfilesQueryExpectations(Object failure) {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        List<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> entities = new ArrayList<>();

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

    protected void setUpEntityQueryExpectations(int times, int index, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetCpuProfileById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[index] },
                    notFound ? null : getEntity(index));
        }
    }

    static CpuProfile getModel(int index) {
        CpuProfile model = new CpuProfile();
        model.setId(GUIDS[index].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    protected List<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> entities =
                new ArrayList<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }

        return entities;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.profiles.CpuProfile getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class),
                index);
    }

    protected void setUpClusterQueryExpectations() {
    }

    static org.ovirt.engine.core.common.businessentities.profiles.CpuProfile setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.profiles.CpuProfile entity,
            int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getClusterId()).andReturn(GUIDS[index]).anyTimes();
        return entity;
    }
}
