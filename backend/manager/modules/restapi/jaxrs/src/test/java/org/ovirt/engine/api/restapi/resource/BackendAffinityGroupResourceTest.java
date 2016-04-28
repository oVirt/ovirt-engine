package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.AffinityGroup;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupResourceTest extends AbstractBackendSubResourceTest<AffinityGroup, org.ovirt.engine.core.common.scheduling.AffinityGroup, BackendAffinityGroupResource> {

    private static final Guid AFFINITY_GROUP_ID = GUIDS[0];
    private static final Guid CLUSTER_ID = GUIDS[0];

    public BackendAffinityGroupResourceTest() {
        super(new BackendAffinityGroupResource(AFFINITY_GROUP_ID.toString()));
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, false);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2, true);

        setUriInfo(setUpActionExpectations(VdcActionType.EditAffinityGroup,
                AffinityGroupCRUDParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        setUpActionExpectations(
            VdcActionType.RemoveAffinityGroup,
            AffinityGroupCRUDParameters.class,
            new String[] { "AffinityGroupId" },
            new Object[] { GUIDS[0] },
            true,
            true
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, false);
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

    private void setUpGetEntityExpectations(int times, boolean found) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetAffinityGroupById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { AFFINITY_GROUP_ID },
                    found ? getEntity(0) : null);
        }
    }

    @Override
    protected org.ovirt.engine.core.common.scheduling.AffinityGroup getEntity(int index) {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                control.createMock(org.ovirt.engine.core.common.scheduling.AffinityGroup.class);
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getClusterId()).andReturn(CLUSTER_ID).anyTimes();
        expect(entity.isEnforcing()).andReturn((GUIDS[index].hashCode() & 1) == 0).anyTimes();
        expect(entity.isPositive()).andReturn((GUIDS[index].hashCode() & 1) == 1).anyTimes();
        return entity;
    }

    static AffinityGroup getModel(int index) {
        AffinityGroup model = new AffinityGroup();
        model.setId(GUIDS[0].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        model.setCluster(new Cluster());
        model.getCluster().setId(CLUSTER_ID.toString());
        model.setEnforcing((GUIDS[index].hashCode() & 1) == 0);
        model.setPositive((GUIDS[index].hashCode() & 1) == 1);

        return model;
    }
}
