package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigRule;


@RunWith(MockitoJUnitRunner.class)
public class PowerSavingBalancePolicyUnitTest extends CpuAndMemoryBalancingPolicyUnitTest {
    static final Guid DESTINATION_HOST = new Guid("087fc691-de02-11e4-8830-0800200c9a66");

    @Mock
    private VdsDao vdsDao;

    @Spy
    SlaValidator slaValidator = new SlaValidator();

    @Spy
    @InjectMocks
    PowerSavingBalancePolicyUnit policyUnit = new PowerSavingBalancePolicyUnit(null, null);

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

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());

        List<Guid> validMigrationTargets = validMigrationTargets(result);
        assertEquals(1, validMigrationTargets.size());
        assertEquals(DESTINATION_HOST, validMigrationTargets.get(0));
    }

    @Test
    public void testBalanceMemoryLoadNoHighMemoryLimitDefined() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertNotNull(result.get().getVmToMigrate());
        List<Guid> validMigrationTargets = validMigrationTargets(result);
        assertEquals(1, validMigrationTargets.size());
        assertEquals(DESTINATION_HOST, validMigrationTargets.get(0));
    }

    @Test
    public void testBalanceMemoryLoadNoLowMemoryLimitDefined() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "600");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        assertNotNull(result.get().getVmToMigrate());
        List<Guid> validMigrationTargets = validMigrationTargets(result);
        assertEquals(1, validMigrationTargets.size());
        assertNotEquals(DESTINATION_HOST, validMigrationTargets.get(0));
    }

    @Test
    public void testBalanceCpuLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertTrue(result.get().isValid());
        List<Guid> validMigrationTargets = validMigrationTargets(result);
        assertEquals(1, validMigrationTargets.size());
        assertEquals(DESTINATION_HOST, validMigrationTargets.get(0));
    }

    @Test
    public void testBalanceCpuAndMemLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpumem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert !result.isPresent();
    }

    @Test
    public void testBalanceMediumLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_medium_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert !result.isPresent();
    }

    @Test
    public void testBalanceNoLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_no_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        ArrayList<String> messages = new ArrayList<>();

        initMocks(policyUnit, hosts, vms);

        Optional<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters, messages);
        assert !result.isPresent();
    }

    @Override
    protected void initMocks(CpuAndMemoryBalancingPolicyUnit unit,
            Map<Guid, VDS> hosts,
            Map<Guid, VM> vms) throws ParseException {

        super.initMocks(unit, hosts, vms);

        doReturn(new ArrayList<>(hosts.values())).when(vdsDao).getAllForCluster(any());

        // disable power management evaluation
        doReturn(null).when(policyUnit).evaluatePowerManagementSituation(any(), any(), any(), any(), any());
    }
}
