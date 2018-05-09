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
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendDiskProfilesResourceTest<C extends AbstractBackendDiskProfilesResource>
        extends AbstractBackendCollectionResourceTest<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile, C> {

    protected static final Guid STORAGE_DOMAIN_ID = GUIDS[1];
    private final QueryType listQueryType;

    public AbstractBackendDiskProfilesResourceTest(C collection, QueryType listQueryType) {
        super(collection, null, "");
        this.listQueryType = listQueryType;
    }

    @Test
    public void testAddDiskProfile() {
        setUriInfo(setUpBasicUriExpectations());
        setUpStorageDomainQueryExpectations();
        setUpCreationExpectations(ActionType.AddDiskProfile,
                DiskProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                QueryType.GetDiskProfileById,
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
    public void testAddDiskProfileCantDo() {
        setUpStorageDomainQueryExpectations();
        doTestBadAddDiskProfile(false, true, CANT_DO);
    }

    @Test
    public void testAddDiskProfileFailure() {
        setUpStorageDomainQueryExpectations();
        doTestBadAddDiskProfile(true, false, FAILURE);
    }

    private void doTestBadAddDiskProfile(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AddDiskProfile,
                DiskProfileParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));
        DiskProfile model = getModel(0);
        model.setStorageDomain(new StorageDomain());
        model.getStorageDomain().setId(STORAGE_DOMAIN_ID.toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        DiskProfile model = createIncompleteDiskProfile();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "DiskProfile", "validateParameters", getIncompleteFields());
    }

    protected String[] getIncompleteFields() {
        return new String[] { "name" };
    }

    protected DiskProfile createIncompleteDiskProfile() {
        return new DiskProfile();
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
        setUpDiskProfilesQueryExpectations(null);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() {
        setUpDiskProfilesQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Override
    @Test
    public void testListCrash() {
        Throwable t = new RuntimeException(FAILURE);
        setUpDiskProfilesQueryExpectations(t);

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
        setUpDiskProfilesQueryExpectations(t);
        collection.setUriInfo(uriInfo);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }

    private void setUpDiskProfilesQueryExpectations(Object failure) {
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> entities = new ArrayList<>();

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

    static DiskProfile getModel(int index) {
        DiskProfile model = new DiskProfile();
        model.setId(GUIDS[index].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.profiles.DiskProfile getEntity(int index) {
        return setUpEntityExpectations(mock(org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class),
                index);
    }

    protected void setUpStorageDomainQueryExpectations() {
    }

    static org.ovirt.engine.core.common.businessentities.profiles.DiskProfile setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getStorageDomainId()).thenReturn(GUIDS[index]);
        return entity;
    }
}
