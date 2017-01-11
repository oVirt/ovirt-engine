package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigRule;


@RunWith(MockitoJUnitRunner.class)
public class PowerSavingBalancePolicyUnitTest extends CpuAndMemoryBalancingPolicyUnitTest {
    static final Guid DESTINATION_HOST = new Guid("087fc691-de02-11e4-8830-0800200c9a66");

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(
                    MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, Integer.MAX_VALUE),
                    MockConfigRule.mockConfig(ConfigValues.HighUtilizationForPowerSave, 80),
                    MockConfigRule.mockConfig(ConfigValues.LowUtilizationForPowerSave, 20),
                    MockConfigRule.mockConfig(ConfigValues.CpuOverCommitDurationMinutes, 5),
                    MockConfigRule.mockConfig(ConfigValues.VcpuConsumptionPercentage, 20),
                    MockConfigRule.mockConfig(ConfigValues.UtilizationThresholdInPercent, 80)
                    );

    @Test
    public void testBalanceMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        PowerSavingBalancePolicyUnit unit = mockUnit(PowerSavingBalancePolicyUnit.class, cluster, hosts, vms);

        // disable power management evaluation
        doReturn(null).when(unit).evaluatePowerManagementSituation(any(Cluster.class), anyList(), anyList(), anyList(), anyMap());

        Optional<BalanceResult> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());

        List<Guid> validMigrationTargets = validMigrationTargets(unit, result);
        assertEquals(1, validMigrationTargets.size());
        assertEquals(DESTINATION_HOST, validMigrationTargets.get(0));
    }

    @Test
    public void testBalanceMemoryLoadNoHighMemoryLimitDefined() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        PowerSavingBalancePolicyUnit unit = mockUnit(PowerSavingBalancePolicyUnit.class, cluster, hosts, vms);

        // disable power management evaluation
        doReturn(null).when(unit)
                .evaluatePowerManagementSituation(any(Cluster.class), anyList(), anyList(), anyList(), anyMap());

        Optional<BalanceResult> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertNotNull(result.get().getVmToMigrate());
        List<Guid> validMigrationTargets = validMigrationTargets(unit, result);
        assertEquals(1, validMigrationTargets.size());
        assertEquals(DESTINATION_HOST, validMigrationTargets.get(0));
    }

    @Test
    public void testBalanceMemoryLoadNoLowMemoryLimitDefined() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "600");

        ArrayList<String> messages = new ArrayList<>();

        PowerSavingBalancePolicyUnit unit = mockUnit(PowerSavingBalancePolicyUnit.class, cluster, hosts, vms);

        // disable power management evaluation
        doReturn(null).when(unit)
                .evaluatePowerManagementSituation(any(Cluster.class), anyList(), anyList(), anyList(), anyMap());

        Optional<BalanceResult> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertNotNull(result.get().getVmToMigrate());
        List<Guid> validMigrationTargets = validMigrationTargets(unit, result);
        assertEquals(1, validMigrationTargets.size());
        assertNotEquals(DESTINATION_HOST, validMigrationTargets.get(0));
    }

    @Test
    public void testBalanceCpuLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        ArrayList<String> messages = new ArrayList<>();

        PowerSavingBalancePolicyUnit unit = mockUnit(PowerSavingBalancePolicyUnit.class, cluster, hosts, vms);

        // disable power management evaluation
        doReturn(null).when(unit).evaluatePowerManagementSituation(any(Cluster.class), anyList(), anyList(), anyList(), anyMap());

        Optional<BalanceResult> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        List<Guid> validMigrationTargets = validMigrationTargets(unit, result);
        assertEquals(1, validMigrationTargets.size());
        assertEquals(DESTINATION_HOST, validMigrationTargets.get(0));
    }

    @Test
    public void testBalanceCpuAndMemLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpumem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        ArrayList<String> messages = new ArrayList<>();

        PowerSavingBalancePolicyUnit unit = mockUnit(PowerSavingBalancePolicyUnit.class, cluster, hosts, vms);

        // disable power management evaluation
        doReturn(null).when(unit).evaluatePowerManagementSituation(any(Cluster.class), anyList(), anyList(), anyList(), anyMap());

        Optional<BalanceResult> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert !result.isPresent();
    }

    @Test
    public void testBalanceMediumLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_medium_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        ArrayList<String> messages = new ArrayList<>();

        PowerSavingBalancePolicyUnit unit = mockUnit(PowerSavingBalancePolicyUnit.class, cluster, hosts, vms);

        // disable power management evaluation
        doReturn(null).when(unit).evaluatePowerManagementSituation(any(Cluster.class), anyList(), anyList(), anyList(), anyMap());

        Optional<BalanceResult> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert !result.isPresent();
    }

    @Test
    public void testBalanceNoLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_no_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Cluster cluster = new Cluster();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        ArrayList<String> messages = new ArrayList<>();

        PowerSavingBalancePolicyUnit unit = mockUnit(PowerSavingBalancePolicyUnit.class, cluster, hosts, vms);

        // disable power management evaluation
        doReturn(null).when(unit).evaluatePowerManagementSituation(any(Cluster.class), anyList(), anyList(), anyList(), anyMap());

        Optional<BalanceResult> result = unit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert !result.isPresent();
    }
}
