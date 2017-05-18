package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;

@RunWith(MockitoJUnitRunner.class)
public class VmAffinityFilterPolicyUnitTest extends VmAffinityPolicyUnitTestBase {

    @InjectMocks
    private VmAffinityFilterPolicyUnit policyUnit = new VmAffinityFilterPolicyUnit(null, pendingResourceManager);

    @Test
    public void testNoAffinityGroups() {
        List<VDS> hosts = Arrays.asList(host1, host2);

        assertThat(policyUnit.filter(cluster, hosts, newVm, null, new PerHostMessages()))
                .containsOnlyElementsOf(hosts);
    }

    @Test
    public void testFirstVm() {
        List<VDS> hosts = Arrays.asList(host1, host2);

        VM vm1 = createVMDown(cluster);
        VM vm2 = createVMDown(cluster);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, true,
                vm1, vm2, newVm));

        assertThat(policyUnit.filter(cluster, hosts, newVm, null, new PerHostMessages()))
                .containsOnlyElementsOf(hosts);

        affinityGroups.clear();
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, true,
                vm1, vm2, newVm));

        assertThat(policyUnit.filter(cluster, hosts, newVm, null, new PerHostMessages()))
                .containsOnlyElementsOf(hosts);
    }

    @Test
    public void testPositiveAffinity() {
        List<VDS> hosts = Arrays.asList(host1, host2);

        VM vm1 = createVmRunning(host2);
        VM vm2 = createVmRunning(host2);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, true,
                vm1, vm2, newVm));

        assertThat(policyUnit.filter(cluster, hosts, newVm, null, new PerHostMessages()))
                .containsOnly(host2);

    }

    @Test
    public void testPositiveAffinityToMultipleHosts() {
        List<VDS> hosts = Arrays.asList(host1, host2, host3);

        VM vm1 = createVmRunning(host2);
        VM vm2 = createVmRunning(host2);
        VM vm3 = createVmRunning(host3);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, true,
                vm1, vm2, vm3, newVm));

        assertThat(policyUnit.filter(cluster, hosts, newVm, null, new PerHostMessages()))
                .containsOnly(host2, host3);
    }

    @Test
    public void testNegativeAffinity() {
        List<VDS> hosts = Arrays.asList(host1, host2, host3);

        VM vm1 = createVmRunning(host1);
        VM vm2 = createVmRunning(host3);


        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, true,
                vm1, vm2, newVm));

        assertThat(policyUnit.filter(cluster, hosts, newVm, null, new PerHostMessages()))
                .doesNotContain(host1, host3);
    }


}
