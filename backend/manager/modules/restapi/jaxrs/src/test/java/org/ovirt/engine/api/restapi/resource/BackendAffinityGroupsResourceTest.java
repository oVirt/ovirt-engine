package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.AffinityGroup;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupsResourceTest extends AbstractBackendCollectionResourceTest<AffinityGroup, org.ovirt.engine.core.common.scheduling.AffinityGroup, BackendAffinityGroupsResource> {
    private static final Guid CLUSTER_ID = GUIDS[0];

    public BackendAffinityGroupsResourceTest() {
        super(new BackendAffinityGroupsResource(CLUSTER_ID.toString()), null, "");
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetAffinityGroupsByClusterId,
                IdQueryParameters.class,
                new String[] {},
                new Object[] {},
                setUpAffinityGroups(),
                failure);

        control.replay();
    }


    @Override
    protected List<AffinityGroup> getCollection() {
        return collection.list().getAffinityGroups();
    }

    @Override
    protected org.ovirt.engine.core.common.scheduling.AffinityGroup getEntity(int index) {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                control.createMock(org.ovirt.engine.core.common.scheduling.AffinityGroup.class);
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getClusterId()).andReturn(CLUSTER_ID).anyTimes();
        expect(entity.isEnforcing()).andReturn(GUIDS[index].hashCode() % 2 == 0).anyTimes();
        expect(entity.isPositive()).andReturn(GUIDS[index].hashCode() % 2 == 1).anyTimes();
        return entity;
    }

    /**
     * Overriding this as the affinity groups collection doesn't support search queries
     */
    @Override
    @Test
    public void testQuery() throws Exception {
        testList();
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddAffinityGroup,
                AffinityGroupCRUDParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                VdcQueryType.GetAffinityGroupById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
        Response response = collection.add(new AffinityGroup());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof AffinityGroup);
        verifyModel((AffinityGroup) response.getEntity(), 0);
    }

    private List<org.ovirt.engine.core.common.scheduling.AffinityGroup> setUpAffinityGroups() {
        List<org.ovirt.engine.core.common.scheduling.AffinityGroup> list = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            list.add(getEntity(i));
        }

        return list;
    }
}
