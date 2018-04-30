package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class EvenDistributionBalancePolicyUnitTest extends CpuAndMemoryBalancingPolicyUnitTest {
    static final Guid DESTINATION_HOST = new Guid("087fc691-de02-11e4-8830-0800200c9a66");

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.HighUtilizationForEvenlyDistribute, 80),
                MockConfigDescriptor.of(ConfigValues.LowUtilizationForEvenlyDistribute, 20),
                MockConfigDescriptor.of(ConfigValues.CpuOverCommitDurationMinutes, 5),
                MockConfigDescriptor.of(ConfigValues.VcpuConsumptionPercentage, 20),
                MockConfigDescriptor.of(ConfigValues.UtilizationThresholdInPercent, 80)
        );
    }

    @Spy
    @InjectMocks
    private EvenDistributionBalancePolicyUnit policyUnit = new EvenDistributionBalancePolicyUnit(null, null);

    @Test
    public void testBalanceCpuLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        initMocks(policyUnit, hosts, vms);

        ArrayList<String> messages = new ArrayList<>();
        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertNotNull(result.get().getVmToMigrate());
        assertEquals(2, result.get().getCandidateHosts().size());
        assertEquals(DESTINATION_HOST, result.get().getCandidateHosts().get(0));
    }

    @Test
    public void testBalanceMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertNotNull(result.get().getVmToMigrate());

        List<Guid> candidateHosts = validMigrationTargets(result);
        assertEquals(1, candidateHosts.size());
        assertEquals(DESTINATION_HOST, candidateHosts.get(0));
    }

    /**
     * Test situation where all hosts are either CPU or Memory over utilized.
     */
    @Test
    public void testBalanceCpuAndMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_cpumem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert !result.isPresent();
    }

    /**
     * Test a scenario where a host is CPU overloaded and the destination has some memory load but
     * not too much so it is able to receive the VM.
     */
    @Test
    public void testBalanceCpuAndMediumMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_cpumem_medium_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "300");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertNotNull(result.get().getVmToMigrate());

        List<Guid> candidateHosts = validMigrationTargets(result);

        assertEquals(1, candidateHosts.size());
        assertEquals(DESTINATION_HOST, candidateHosts.get(0));
    }

    /**
     * Test a scenario where a host is CPU overloaded and the destination has some memory load but
     * it is too much to receive the VM.
     */
    @Test
    public void testBalanceCpuAndHighMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_cpumem_medium_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert !result.isPresent();
    }
}
