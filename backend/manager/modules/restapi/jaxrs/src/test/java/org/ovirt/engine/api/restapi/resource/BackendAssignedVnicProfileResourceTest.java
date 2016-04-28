package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendAssignedVnicProfileResourceTest
        extends AbstractBackendVnicProfileResourceTest<BackendAssignedVnicProfileResource> {

    public BackendAssignedVnicProfileResourceTest() {
        super(new BackendAssignedVnicProfileResource(GUIDS[0].toString(),
                new BackendAssignedVnicProfilesResource(GUIDS[0].toString())));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
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
        control.replay();
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
        control.replay();

        verifyModel(resource.get(), 0);
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

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.VnicProfileView getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VnicProfileView.class), index);
    }

    static VnicProfileView setUpEntityExpectations(VnicProfileView entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getNetworkId()).andReturn(GUIDS[index]).anyTimes();
        return entity;
    }
}
