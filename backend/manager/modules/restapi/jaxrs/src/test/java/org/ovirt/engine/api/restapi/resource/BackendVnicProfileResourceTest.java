package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendVnicProfileResourceTest
        extends AbstractBackendVnicProfileResourceTest<BackendVnicProfileResource> {

    public BackendVnicProfileResourceTest() {
        super(new BackendVnicProfileResource(GUIDS[0].toString()));
    }

    @Test
    public void testBadGuid() throws Exception {
        try {
            new BackendVnicProfileResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, true);
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, false);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, true);
        BackendVnicProfileResource resource = (BackendVnicProfileResource) this.resource;
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(2, 0, false);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVnicProfile,
                VnicProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));
        BackendVnicProfileResource resource = (BackendVnicProfileResource) this.resource;
        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(1, 0, false);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVnicProfile,
                VnicProfileParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));

        try {
            BackendVnicProfileResource resource = (BackendVnicProfileResource) this.resource;
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, false);

        VnicProfile model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            BackendVnicProfileResource resource = (BackendVnicProfileResource) this.resource;
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected void setUpEntityQueryExpectations(int times, int index, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVnicProfileById,
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
