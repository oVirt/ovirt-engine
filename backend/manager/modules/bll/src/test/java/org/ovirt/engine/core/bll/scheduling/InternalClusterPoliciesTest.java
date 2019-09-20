package org.ovirt.engine.core.bll.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.scheduling.policyunits.CPUPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HaReservationWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MemoryPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityWeightPolicyUnit;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class InternalClusterPoliciesTest {
    @Test
    public void testConfiguredPolicyCreation() {
        assertNotNull(InternalClusterPolicies.getClusterPolicies());
        assertNotEquals(0, (long)InternalClusterPolicies.getClusterPolicies().size());
    }

    @Test
    public void testDefaultPolicy() {
        assertNotNull(InternalClusterPolicies.getClusterPolicies());
        long defaultPolicies = InternalClusterPolicies.getClusterPolicies().values().stream()
                .filter(ClusterPolicy::isDefaultPolicy)
                .count();
        assertEquals(1, defaultPolicies, "There can be only one default InternalClusterPolicy");
    }

    @Test
    public void testFailureToAddUnitBadType() {
        assertThrows(IllegalArgumentException.class, () ->
                InternalClusterPolicies.createBuilder(UUID.randomUUID().toString())
                        .addFunction(1, InternalPolicyUnitsTest.DummyUnit.class)
                        .getPolicy());
    }

    @Test
    public void testFailureToAddUnitNotEnabled() {
        assertThrows(IllegalArgumentException.class, () ->
                InternalClusterPolicies.createBuilder(UUID.randomUUID().toString())
                        .addFilters(InternalPolicyUnitsTest.NotEnabledDummyUnit.class)
                        .getPolicy());
    }

    @Test
    public void testPolicyCreation() {
        Guid uuid = Guid.newGuid();
        ClusterPolicy policy = InternalClusterPolicies.createBuilder(uuid.toString())
                .name("test-policy")
                .isDefault()
                .description("test-description")
                .set(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES, "5")
                .set(PolicyUnitParameter.SPM_VM_GRACE, "1")
                .setBalancer(EvenDistributionBalancePolicyUnit.class)
                .addFilters(CPUPolicyUnit.class)
                .addFilters(MemoryPolicyUnit.class)
                .addFilters(VmAffinityFilterPolicyUnit.class)
                .addFunction(1, HaReservationWeightPolicyUnit.class)
                .addFunction(2, VmAffinityWeightPolicyUnit.class)
                .getPolicy();

        assertNotNull(policy);
        assertEquals(uuid, policy.getId());
        assertEquals("test-policy", policy.getName());
        assertEquals("test-description", policy.getDescription());
        assertTrue(policy.isDefaultPolicy());
        assertTrue(policy.isLocked());

        assertNotNull(policy.getFilterPositionMap());
        assertEquals(-1L, (long) policy.getFilterPositionMap().get(getUnitId(CPUPolicyUnit.class)));
        assertEquals(0, (long) policy.getFilterPositionMap().get(getUnitId(MemoryPolicyUnit.class)));
        assertEquals(1L, (long) policy.getFilterPositionMap().get(getUnitId(VmAffinityFilterPolicyUnit.class)));

        assertNotNull(policy.getParameterMap());
        assertEquals(2, policy.getParameterMap().size());
        assertEquals("5", policy.getParameterMap().get(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES.getDbName()));
        assertEquals("1", policy.getParameterMap().get(PolicyUnitParameter.SPM_VM_GRACE.getDbName()));

        assertNotNull(policy.getFunctions());
        assertNotNull(policy.getFilters());

        assertEquals(3, policy.getFilters().size());
        assertEquals(2, policy.getFunctions().size());

        Map<Guid, Integer> funcMap = new HashMap<>();
        for (Pair<Guid, Integer> pair: policy.getFunctions()) {
            funcMap.put(pair.getFirst(), pair.getSecond());
        }

        assertEquals(1, (long)funcMap.get(getUnitId(HaReservationWeightPolicyUnit.class)));
        assertEquals(2, (long)funcMap.get(getUnitId(VmAffinityWeightPolicyUnit.class)));
    }

    private Guid getUnitId(Class<? extends PolicyUnitImpl> unit) {
        return Guid.createGuidFromString(unit.getAnnotation(SchedulingUnit.class).guid());
    }
}
