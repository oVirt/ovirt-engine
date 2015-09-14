package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupVmResourceTest
        extends AbstractBackendSubResourceTest<Vm, org.ovirt.engine.core.common.businessentities.VM, BackendAffinityGroupVmResource> {

    public BackendAffinityGroupVmResourceTest() {
        super(new BackendAffinityGroupVmResource(GUIDS[0], GUIDS[1].toString()));
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetGroupExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.EditAffinityGroup,
                AffinityGroupCRUDParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    private void setUpGetGroupExpectations() throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetAffinityGroupById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getGroup()
        );
    }

    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        vm.setId(GUIDS[index]);
        vm.setName(NAMES[index]);
        return vm;
    }

    private org.ovirt.engine.core.common.scheduling.AffinityGroup getGroup() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup group =
            new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        List<Guid> vmIds = new ArrayList<>();
        List<String> vmNames = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            org.ovirt.engine.core.common.businessentities.VM vm = getEntity(i);
            vmIds.add(vm.getId());
            vmNames.add(vm.getName());
        }
        group.setEntityIds(vmIds);
        group.setEntityNames(vmNames);
        return group;
    }
}
