package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class EvenDistributionBalancePolicyUnitTest extends CpuAndMemoryBalancingPolicyUnitTest {
    static final Guid DESTINATION_HOST = new Guid("087fc691-de02-11e4-8830-0800200c9a66");

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(
                    MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, Integer.MAX_VALUE),
                    MockConfigRule.mockConfig(ConfigValues.HighUtilizationForEvenlyDistribute, 80),
                    MockConfigRule.mockConfig(ConfigValues.LowUtilizationForEvenlyDistribute, 20),
                    MockConfigRule.mockConfig(ConfigValues.CpuOverCommitDurationMinutes, 5),
                    MockConfigRule.mockConfig(ConfigValues.VcpuConsumptionPercentage, 20),
                    MockConfigRule.mockConfig(ConfigValues.UtilizationThresholdInPercent, 80)
            );

    @Test
    public void testBalanceCpuLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        EvenDistributionBalancePolicyUnit unit = mockUnit(EvenDistributionBalancePolicyUnit.class, cluster, hosts, vms);

        Pair<List<Guid>, Guid> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertNotNull(result.getSecond());
        assertEquals(result.getFirst().size(), 1);
        assertEquals(result.getFirst().get(0), DESTINATION_HOST);
    }

    @Test
    public void testBalanceMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        EvenDistributionBalancePolicyUnit unit = mockUnit(EvenDistributionBalancePolicyUnit.class, cluster, hosts, vms);

        Pair<List<Guid>, Guid> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertNotNull(result.getSecond());
        assertEquals(result.getFirst().size(), 1);
        assertEquals(result.getFirst().get(0), DESTINATION_HOST);
    }

    /**
     * Test situation where all hosts are either CPU or Memory over utilized.
     */
    @Test
    public void testBalanceCpuAndMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_cpumem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        EvenDistributionBalancePolicyUnit unit = mockUnit(EvenDistributionBalancePolicyUnit.class, cluster, hosts, vms);

        Pair<List<Guid>, Guid> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert result == null;
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

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "300");

        ArrayList<String> messages = new ArrayList<>();

        EvenDistributionBalancePolicyUnit unit = mockUnit(EvenDistributionBalancePolicyUnit.class, cluster, hosts, vms);

        Pair<List<Guid>, Guid> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertNotNull(result.getSecond());
        assertEquals(result.getFirst().size(), 1);
        assertEquals(result.getFirst().get(0), DESTINATION_HOST);
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

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        EvenDistributionBalancePolicyUnit unit = mockUnit(EvenDistributionBalancePolicyUnit.class, cluster, hosts, vms);

        Pair<List<Guid>, Guid> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert result == null;
    }
}
