package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmAffinityWeightPolicyUnitTest extends VmAffinityPolicyUnitTestBase {

    @InjectMocks
    private VmAffinityWeightPolicyUnit policyUnit = new VmAffinityWeightPolicyUnit(null, pendingResourceManager);

    @Test
    public void testNoAffinityGroups() {
        List<VDS> hosts = Arrays.asList(host1, host2);

        Map<Guid, Integer> scores = collectScores(hosts);
        assertEquals(scores.get(host1.getId()), scores.get(host2.getId()));
    }

    @Test
    public void testFirstVm() {
        List<VDS> hosts = Arrays.asList(host1, host2);

        VM vm1 = createVMDown(cluster);
        VM vm2 = createVMDown(cluster);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, true,
                vm1, vm2, newVm));

        Map<Guid, Integer> scores = collectScores(hosts);
        assertEquals(scores.get(host1.getId()), scores.get(host2.getId()));

        affinityGroups.clear();
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, false,
                vm1, vm2, newVm));

        scores = collectScores(hosts);
        assertEquals(scores.get(host1.getId()), scores.get(host2.getId()));
    }

    @Test
    public void testPositiveAffinity() {
        List<VDS> hosts = Arrays.asList(host1, host2, host3);

        VM vm1 = createVmRunning(host2);
        VM vm2 = createVmRunning(host2);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, true,
                vm1, vm2, newVm));

        Map<Guid, Integer> scores = collectScores(hosts);
        assertEquals(scores.get(host1.getId()), scores.get(host3.getId()));
        assertThat(scores.get(host1.getId())).isGreaterThan(scores.get(host2.getId()));

        affinityGroups.clear();
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, false,
                vm1, vm2, newVm));

        scores = collectScores(hosts);
        assertEquals(scores.get(host1.getId()), scores.get(host3.getId()));
        assertThat(scores.get(host1.getId())).isGreaterThan(scores.get(host2.getId()));
    }

    @Test
    public void testPositiveAffinityWithPinnedOrHE() {
        List<VDS> hosts = Arrays.asList(host1, host2, host3);

        VM vm1 = createVmRunning(host2);
        VM vm2 = createVmRunning(host3);
        vm1.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, true,
                vm1, vm2, newVm));

        Map<Guid, Integer> scores = collectScores(hosts);
        assertThat(scores.get(host2.getId())).isLessThan(scores.get(host3.getId()));
        assertThat(scores.get(host3.getId())).isLessThan(scores.get(host1.getId()));


        vm1.setMigrationSupport(MigrationSupport.MIGRATABLE);
        vm1.setOrigin(OriginType.HOSTED_ENGINE);

        scores = collectScores(hosts);
        assertThat(scores.get(host2.getId())).isLessThan(scores.get(host3.getId()));
        assertThat(scores.get(host3.getId())).isLessThan(scores.get(host1.getId()));

        vm1.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vm1.setOrigin(OriginType.RHEV);

        affinityGroups.clear();
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, false,
                vm1, vm2, newVm));

        scores = collectScores(hosts);
        assertThat(scores.get(host2.getId())).isLessThan(scores.get(host3.getId()));
        assertThat(scores.get(host3.getId())).isLessThan(scores.get(host1.getId()));

        vm1.setMigrationSupport(MigrationSupport.MIGRATABLE);
        vm1.setOrigin(OriginType.HOSTED_ENGINE);

        scores = collectScores(hosts);
        assertThat(scores.get(host2.getId())).isLessThan(scores.get(host3.getId()));
        assertThat(scores.get(host3.getId())).isLessThan(scores.get(host1.getId()));
    }

    @Test
    public void testNegativeAffinity() {
        List<VDS> hosts = Arrays.asList(host1, host2, host3);

        VM vm1 = createVmRunning(host1);
        VM vm2 = createVmRunning(host3);


        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, true,
                vm1, vm2, newVm));

        Map<Guid, Integer> scores = collectScores(hosts);
        assertEquals(scores.get(host1.getId()), scores.get(host3.getId()));
        assertThat(scores.get(host1.getId())).isGreaterThan(scores.get(host2.getId()));

        affinityGroups.clear();
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, false,
                vm1, vm2, newVm));

        scores = collectScores(hosts);
        assertEquals(scores.get(host1.getId()), scores.get(host3.getId()));
        assertThat(scores.get(host1.getId())).isGreaterThan(scores.get(host2.getId()));
    }

    @Test
    public void testPositiveAffinityWithPriority() {
        List<VDS> hosts = Arrays.asList(host1, host2, host3);
        VM vm1 = createVmRunning(host1);
        VM vm2 = createVmRunning(host2);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, false, 1.0,
                vm1, newVm));
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, false, 1.5,
                vm2, newVm));

        Map<Guid, Integer> scores = collectScores(hosts);
        assertThat(scores.get(host2.getId())).isLessThan(scores.get(host1.getId()));
        assertThat(scores.get(host1.getId())).isLessThan(scores.get(host3.getId()));
    }

    @Test
    public void testNegativeAffinityWithPriority() {
        List<VDS> hosts = Arrays.asList(host1, host2, host3);
        VM vm1 = createVmRunning(host1);
        VM vm2 = createVmRunning(host2);

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, false, 1.0,
                vm1, newVm));
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, false, 1.5,
                vm2, newVm));

        Map<Guid, Integer> scores = collectScores(hosts);
        assertThat(scores.get(host3.getId())).isLessThan(scores.get(host1.getId()));
        assertThat(scores.get(host1.getId())).isLessThan(scores.get(host2.getId()));
    }

    private Map<Guid, Integer> collectScores(List<VDS> hosts) {
        return policyUnit.score(context, hosts, Collections.singletonList(newVm)).stream()
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }
}
