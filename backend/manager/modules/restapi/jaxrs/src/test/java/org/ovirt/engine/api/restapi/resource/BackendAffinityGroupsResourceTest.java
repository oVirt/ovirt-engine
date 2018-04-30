package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.AffinityGroup;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAffinityGroupsResourceTest extends AbstractBackendCollectionResourceTest<AffinityGroup, org.ovirt.engine.core.common.scheduling.AffinityGroup, BackendAffinityGroupsResource> {
    private static final Guid CLUSTER_ID = GUIDS[0];

    public BackendAffinityGroupsResourceTest() {
        super(new BackendAffinityGroupsResource(CLUSTER_ID.toString()), null, "");
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetAffinityGroupsByClusterId,
                IdQueryParameters.class,
                new String[] {},
                new Object[] {},
                setUpAffinityGroups(),
                failure);

    }


    @Override
    protected List<AffinityGroup> getCollection() {
        return collection.list().getAffinityGroups();
    }

    @Override
    protected org.ovirt.engine.core.common.scheduling.AffinityGroup getEntity(int index) {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                mock(org.ovirt.engine.core.common.scheduling.AffinityGroup.class);
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getClusterId()).thenReturn(CLUSTER_ID);
        when(entity.isVmEnforcing()).thenReturn(GUIDS[index].hashCode() % 2 == 0);
        when(entity.isVmPositive()).thenReturn(GUIDS[index].hashCode() % 2 == 1);
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
        setUpCreationExpectations(ActionType.AddAffinityGroup,
                AffinityGroupCRUDParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                QueryType.GetAffinityGroupById,
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
