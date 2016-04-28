package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendAssignedCpuProfileResourceTest
        extends AbstractBackendSubResourceTest<CpuProfile, org.ovirt.engine.core.common.businessentities.profiles.CpuProfile, BackendAssignedCpuProfileResource> {

    public BackendAssignedCpuProfileResourceTest() {
        super(new BackendAssignedCpuProfileResource(GUIDS[0].toString(),
                new BackendAssignedCpuProfilesResource(GUIDS[0].toString())));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendCpuProfileResource("foo");
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

    @Test
    public void testRemoveNotFound() throws Exception {
        setUpEntityQueryExpectations(1, 0, true);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(2, 0, false);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveCpuProfile,
                CpuProfileParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetCpuProfileById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            null
        );
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
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

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(2, 0, false);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveCpuProfile,
                CpuProfileParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
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

    @Override
    protected org.ovirt.engine.core.common.businessentities.profiles.CpuProfile getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class),
                index);
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
