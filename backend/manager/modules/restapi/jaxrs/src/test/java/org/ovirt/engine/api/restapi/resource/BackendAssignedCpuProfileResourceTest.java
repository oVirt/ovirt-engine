package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.CpuProfile;
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

    @Override
    protected org.ovirt.engine.core.common.businessentities.profiles.CpuProfile getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class),
                index);
    }

    protected List<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> entities =
                new ArrayList<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }

        return entities;
    }

    static org.ovirt.engine.core.common.businessentities.profiles.CpuProfile setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.profiles.CpuProfile entity,
            int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getClusterId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getQosId()).andReturn(GUIDS[index]).anyTimes();
        return entity;
    }
}
