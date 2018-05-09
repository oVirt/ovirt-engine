package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVnicProfileResourceTest
        extends AbstractBackendVnicProfileResourceTest<BackendVnicProfileResource> {

    public BackendVnicProfileResourceTest() {
        super(new BackendVnicProfileResource(GUIDS[0].toString()));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException
                (assertThrows(WebApplicationException.class, () -> new BackendVnicProfileResource("foo")));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, false);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, true);
        BackendVnicProfileResource resource = (BackendVnicProfileResource) this.resource;
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpEntityQueryExpectations(2, 0, false);

        setUriInfo(setUpActionExpectations(ActionType.UpdateVnicProfile,
                VnicProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));
        BackendVnicProfileResource resource = (BackendVnicProfileResource) this.resource;
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
        setUpEntityQueryExpectations(1, 0, false);

        setUriInfo(setUpActionExpectations(ActionType.UpdateVnicProfile,
                VnicProfileParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));

        verifyFault(
                assertThrows(
                        WebApplicationException.class,
                        () -> ((BackendVnicProfileResource) this.resource).update(getModel(0))
                ), detail
        );
    }

    @Test
    public void testConflictedUpdate() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, false);

        VnicProfile model = getModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(
                WebApplicationException.class, () -> ((BackendVnicProfileResource) resource).update(model)));
    }

    protected void setUpEntityQueryExpectations(int times, int index, boolean notFound) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetVnicProfileById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[index] },
                    notFound ? null : getEntity(index));
        }
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

    static org.ovirt.engine.core.common.businessentities.network.VnicProfile setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.network.VnicProfile entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getNetworkId()).thenReturn(GUIDS[index]);
        return entity;
    }
}
