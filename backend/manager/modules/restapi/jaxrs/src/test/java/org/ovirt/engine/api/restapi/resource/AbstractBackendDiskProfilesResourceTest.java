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
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendDiskProfilesResourceTest<C extends AbstractBackendDiskProfilesResource>
        extends AbstractBackendCollectionResourceTest<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile, C> {

    protected static final Guid STORAGE_DOMAIN_ID = GUIDS[1];
    private final VdcQueryType listQueryType;
    private final Class<? extends VdcQueryParametersBase> listQueryParamsClass;

    public AbstractBackendDiskProfilesResourceTest(C collection,
            VdcQueryType listQueryType,
            Class<? extends VdcQueryParametersBase> queryParamsClass) {
        super(collection, null, "");
        this.listQueryType = listQueryType;
        this.listQueryParamsClass = queryParamsClass;
    }

    @Test
    public void testAddDiskProfile() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpStorageDomainQueryExpectations();
        setUpCreationExpectations(VdcActionType.AddDiskProfile,
                DiskProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                VdcQueryType.GetDiskProfileById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { Guid.Empty },
                getEntity(0));
        DiskProfile model = getModel(0);
        model.setStorageDomain(new StorageDomain());
        model.getStorageDomain().setId(STORAGE_DOMAIN_ID.toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof DiskProfile);
        verifyModel((DiskProfile) response.getEntity(), 0);
    }

    @Test
    public void testAddDiskProfileCantDo() throws Exception {
        setUpStorageDomainQueryExpectations();
        doTestBadAddDiskProfile(false, true, CANT_DO);
    }

    @Test
    public void testAddDiskProfileFailure() throws Exception {
        setUpStorageDomainQueryExpectations();
        doTestBadAddDiskProfile(true, false, FAILURE);
    }

    private void doTestBadAddDiskProfile(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddDiskProfile,
                DiskProfileParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));
        DiskProfile model = getModel(0);
        model.setStorageDomain(new StorageDomain());
        model.getStorageDomain().setId(STORAGE_DOMAIN_ID.toString());

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        DiskProfile model = createIncompleteDiskProfile();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "DiskProfile", "validateParameters", getIncompleteFields());
        }
    }

    protected String[] getIncompleteFields() {
        return new String[] { "name" };
    }

    protected DiskProfile createIncompleteDiskProfile() {
        return new DiskProfile();
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
        setUpDiskProfilesQueryExpectations(null);
        control.replay();
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() throws Exception {
        setUpDiskProfilesQueryExpectations(FAILURE);
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
        setUpDiskProfilesQueryExpectations(t);

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
        setUpDiskProfilesQueryExpectations(t);
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

    private void setUpDiskProfilesQueryExpectations(Object failure) {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> entities = new ArrayList<>();

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

    static DiskProfile getModel(int index) {
        DiskProfile model = new DiskProfile();
        model.setId(GUIDS[index].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.profiles.DiskProfile getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class),
                index);
    }

    protected void setUpStorageDomainQueryExpectations() {
    }

    static org.ovirt.engine.core.common.businessentities.profiles.DiskProfile setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity,
            int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getStorageDomainId()).andReturn(GUIDS[index]).anyTimes();
        return entity;
    }
}
