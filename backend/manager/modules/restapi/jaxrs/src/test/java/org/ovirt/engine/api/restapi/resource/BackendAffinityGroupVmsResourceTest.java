package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupVmsResourceTest
        extends AbstractBackendCollectionResourceTest<VM, org.ovirt.engine.core.common.businessentities.VM, BackendAffinityGroupVmsResource> {

    private static final Guid AFFINITY_GROUP_ID = Guid.newGuid();

    public BackendAffinityGroupVmsResourceTest() {
        super(new BackendAffinityGroupVmsResource(AFFINITY_GROUP_ID), null, "");
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        assertEquals("", query);

        setUpGetEntityExpectations(true);
        control.replay();
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
    public void testAddVMToAffinityGroup() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);

        setUriInfo(setUpActionExpectations(VdcActionType.EditAffinityGroup,
                AffinityGroupCRUDParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        VM vm = new VM();
        vm.setId(GUIDS[0].toString());
        Response response = collection.add(vm);
        assertEquals(200, response.getStatus());
    }


    private void setUpGetEntityExpectations(boolean withVms) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetAffinityGroupById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { AFFINITY_GROUP_ID },
                getAffinityGroup(withVms));
    }

    private org.ovirt.engine.core.common.scheduling.AffinityGroup getAffinityGroup(boolean withVms) {
        org.ovirt.engine.core.common.scheduling.AffinityGroup affinityGroup = new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        affinityGroup.setEntityIds(new ArrayList<Guid>());
        affinityGroup.setEntityNames(new ArrayList<String>());
        if (withVms) {
            for (int i = 0; i < NAMES.length; i++) {
                org.ovirt.engine.core.common.businessentities.VM entity = getEntity(i);
                affinityGroup.getEntityIds().add(entity.getId());
                affinityGroup.getEntityNames().add(entity.getName());
            }
        }

        return affinityGroup;
    }

    @Override
    protected List<VM> getCollection() {
        return collection.list().getVMs();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetAffinityGroupById,
                IdQueryParameters.class,
                new String[] {},
                new Object[] {},
                null,
                failure);

        control.replay();
    }

    @Override
    protected void verifyModel(VM model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        // overriding since vm doesn't has description
        //assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VM vm =
                new org.ovirt.engine.core.common.businessentities.VM();
        vm.setId(GUIDS[index]);
        vm.setName(NAMES[index]);

        return vm;
    }

}
