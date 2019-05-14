package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class VmToHostAffinityWeightPolicyUnitTest extends VmToHostAffinityPolicyUnitBaseTest {

    @InjectMocks
    VmToHostAffinityWeightPolicyUnit unit =
            new VmToHostAffinityWeightPolicyUnit(null, null);

    @Test
    public void testNoAffinityViolationScore() {
        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = new ArrayList<>();
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();

        assertEquals(1, (long) results.get(host_positive_enforcing.getId()));
        assertEquals(1, (long) results.get(host_negative_enforcing.getId()));
        assertEquals(1, (long) results.get(host_not_in_affinity_group.getId()));
    }

    @Test
    public void testPositiveAffinityViolationScore() {
        hosts = Arrays.asList(host_positive_enforcing, host_not_in_affinity_group);

        positive_enforcing_group.setVdsEnforcing(false);
        List<AffinityGroup> affinityGroups = Arrays.asList(positive_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();
        assertEquals(1, (long) results.get(host_positive_enforcing.getId()));
        assertEquals(2, (long) results.get(host_not_in_affinity_group.getId()));
    }

    @Test
    public void testNegativeAffinityViolationScore() {
        hosts = Arrays.asList(host_negative_enforcing, host_not_in_affinity_group);

        negative_enforcing_group.setVdsEnforcing(false);
        List<AffinityGroup> affinityGroups = Arrays.asList(negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();
        assertEquals(2, (long) results.get(host_negative_enforcing.getId()));
        assertEquals(1, (long) results.get(host_not_in_affinity_group.getId()));
    }

    @Test
    public void testNegativeAndPositiveAffinityViolationScore() {
        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        positive_enforcing_group.setVdsEnforcing(false);
        negative_enforcing_group.setVdsEnforcing(false);

        List<AffinityGroup> affinityGroups = Arrays.asList(positive_enforcing_group, negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();
        assertEquals(3, (long) results.get(host_negative_enforcing.getId()));
        assertEquals(2, (long) results.get(host_not_in_affinity_group.getId()));
    }

    @Test
    public void testNegativeAndPositiveAffinityViolationScoreWithRunningVm() {
        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        positive_enforcing_group.setVdsEnforcing(false);
        negative_enforcing_group.setVdsEnforcing(false);

        vm.setRunOnVds(host_not_in_affinity_group.getId());

        List<AffinityGroup> affinityGroups = Arrays.asList(positive_enforcing_group, negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();
        assertEquals(3, (long) results.get(host_negative_enforcing.getId()));
        assertEquals(2, (long) results.get(host_not_in_affinity_group.getId()));
    }


    @Test
    public void testPositiveAffinityWithPriority() {
        VDS host1 = createHost(cluster);
        VDS host2 = createHost(cluster);
        VDS host3 = createHost(cluster);

        hosts = Arrays.asList(host1, host2, host3);

        List<AffinityGroup> groups = Arrays.asList(
                createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, false, 1.0,
                        Collections.singletonList(vm), Collections.singletonList(host1)),
                createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, false, 1.5,
                        Collections.singletonList(vm), Collections.singletonList(host2))
        );

        doReturn(groups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();
        assertThat(results.get(host2.getId())).isLessThan(results.get(host1.getId()));
        assertThat(results.get(host1.getId())).isLessThan(results.get(host3.getId()));
    }

    @Test
    public void testNegativeAffinityWithPriority() {
        VDS host1 = createHost(cluster);
        VDS host2 = createHost(cluster);
        VDS host3 = createHost(cluster);

        hosts = Arrays.asList(host1, host2, host3);

        List<AffinityGroup> groups = Arrays.asList(
                createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, false, 1.0,
                        Collections.singletonList(vm), Collections.singletonList(host1)),
                createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, false, 1.5,
                        Collections.singletonList(vm), Collections.singletonList(host2))
        );

        doReturn(groups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();
        assertThat(results.get(host3.getId())).isLessThan(results.get(host1.getId()));
        assertThat(results.get(host1.getId())).isLessThan(results.get(host2.getId()));
    }

    private Map<Guid, Integer> getScoreResults() {
        List<Pair<Guid, Integer>> weights = unit.score(context, hosts, vm);
        Map<Guid, Integer> results = weights.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return results;
    }

}
