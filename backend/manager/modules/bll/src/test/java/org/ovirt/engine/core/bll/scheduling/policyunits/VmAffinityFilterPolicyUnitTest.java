package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmAffinityFilterPolicyUnitTest extends VmAffinityPolicyUnitTestBase {

    @InjectMocks
    private VmAffinityFilterPolicyUnit policyUnit = new VmAffinityFilterPolicyUnit(null, pendingResourceManager);

    @Test
    public void testNoAffinityGroups() {
        List<VDS> hosts = Arrays.asList(host1, host2);

        assertThat(filter(hosts))
                .containsOnlyElementsOf(hosts);
    }

    @Test
    public void testFirstVm() {
        List<VDS> hosts = Arrays.asList(host1, host2);

        VM vm1 = createVMDown(cluster);
        VM vm2 = createVMDown(cluster);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, true,
                vm1, vm2, newVm));

        assertThat(filter(hosts))
                .containsOnlyElementsOf(hosts);

        affinityGroups.clear();
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, true,
                vm1, vm2, newVm));

        assertThat(filter(hosts))
                .containsOnlyElementsOf(hosts);
    }

    @Test
    public void testPositiveAffinity() {
        List<VDS> hosts = Arrays.asList(host1, host2);

        VM vm1 = createVmRunning(host2);
        VM vm2 = createVmRunning(host2);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, true,
                vm1, vm2, newVm));

        assertThat(filter(hosts))
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

        assertThat(filter(hosts))
                .containsOnly(host2, host3);
    }

    @Test
    public void testNegativeAffinity() {
        List<VDS> hosts = Arrays.asList(host1, host2, host3);

        VM vm1 = createVmRunning(host1);
        VM vm2 = createVmRunning(host3);


        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, true,
                vm1, vm2, newVm));

        assertThat(filter(hosts))
                .doesNotContain(host1, host3);
    }

    private List<VDS> filter(List<VDS> hosts) {
        return policyUnit.filter(context, hosts, Collections.singletonList(newVm), new PerHostMessages());
    }
}
