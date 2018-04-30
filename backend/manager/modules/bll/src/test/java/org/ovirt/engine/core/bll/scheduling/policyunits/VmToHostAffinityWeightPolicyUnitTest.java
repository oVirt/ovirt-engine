package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
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
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

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
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();
        assertEquals(1, (long) results.get(host_positive_enforcing.getId()));
        assertEquals(2, (long) results.get(host_not_in_affinity_group.getId()));
    }

    @Test
    public void testNegativeAffinityViolationScore() {
        hosts = Arrays.asList(host_negative_enforcing, host_not_in_affinity_group);

        negative_enforcing_group.setVdsEnforcing(false);
        List<AffinityGroup> affinityGroups = Arrays.asList(negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

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
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

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
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

        Map<Guid, Integer> results = getScoreResults();
        assertEquals(4, (long) results.get(host_negative_enforcing.getId()));
        assertEquals(2, (long) results.get(host_not_in_affinity_group.getId()));
    }


    private Map<Guid, Integer> getScoreResults() {
        List<Pair<Guid, Integer>> weights = unit.score(cluster, hosts, vm, new HashMap<>());
        Map<Guid, Integer> results = weights.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return results;
    }

}
