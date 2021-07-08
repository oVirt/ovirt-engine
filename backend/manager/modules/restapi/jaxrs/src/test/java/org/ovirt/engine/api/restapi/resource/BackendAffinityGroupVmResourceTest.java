package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupMemberChangeParameters;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAffinityGroupVmResourceTest
        extends AbstractBackendSubResourceTest<Vm, org.ovirt.engine.core.common.businessentities.VM, BackendAffinityGroupVmResource> {

    public BackendAffinityGroupVmResourceTest() {
        super(new BackendAffinityGroupVmResource(GUIDS[0], GUIDS[1].toString()));
    }

    @Test
    public void testRemove() {
        setUriInfo(setUpBasicUriExpectations());
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveVmFromAffinityGroup,
                AffinityGroupMemberChangeParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true
            )
        );
        verifyRemove(resource.remove());
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
        group.setVmIds(vmIds);
        group.setVmEntityNames(vmNames);
        return group;
    }
}
