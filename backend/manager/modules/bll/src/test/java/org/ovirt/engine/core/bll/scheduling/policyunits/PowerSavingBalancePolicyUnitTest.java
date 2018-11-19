package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;


@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class PowerSavingBalancePolicyUnitTest extends CpuAndMemoryBalancingPolicyUnitTest {
    private static final Guid HOST_A = new Guid("087fc690-de02-11e4-8830-0800200c9a66");
    private static final Guid HOST_B = new Guid("087fc691-de02-11e4-8830-0800200c9a66");
    private static final Guid HOST_C = new Guid("087fc692-de02-11e4-8830-0800200c9a66");

    private static final Guid VM_1 = new Guid("087fc692-de02-11e4-8830-0800200c9a66");
    private static final Guid VM_2 = new Guid("087fc693-de02-11e4-8830-0800200c9a66");
    private static final Guid VM_3 = new Guid("087fc694-de02-11e4-8830-0800200c9a66");

    @Mock
    private VdsDao vdsDao;

    @Spy
    @InjectMocks
    PowerSavingBalancePolicyUnit policyUnit = new PowerSavingBalancePolicyUnit(null, null);

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.HighUtilizationForPowerSave, 80),
                MockConfigDescriptor.of(ConfigValues.LowUtilizationForPowerSave, 20),
                MockConfigDescriptor.of(ConfigValues.CpuOverCommitDurationMinutes, 5),
                MockConfigDescriptor.of(ConfigValues.VcpuConsumptionPercentage, 20),
                MockConfigDescriptor.of(ConfigValues.UtilizationThresholdInPercent, 80)
        );
    }

    @Test
    public void testBalanceMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        initMocks(policyUnit, hosts, vms);

        List<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertThat(result).hasSize(2);
        assertBalanceResult(VM_1, Arrays.asList(HOST_A, HOST_B, HOST_C), result.get(0));
        assertBalanceResult(VM_3, Arrays.asList(HOST_B, HOST_C), result.get(1));
    }

    @Test
    public void testBalanceMemoryLoadNoHighMemoryLimitDefined() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "512");

        initMocks(policyUnit, hosts, vms);

        List<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertThat(result).hasSize(1);
        assertBalanceResult(VM_1, Arrays.asList(HOST_A, HOST_B, HOST_C), result.get(0));
    }

    @Test
    public void testBalanceMemoryLoadNoLowMemoryLimitDefined() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");

        initMocks(policyUnit, hosts, vms);

        List<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertThat(result).hasSize(1);
        assertBalanceResult(VM_3, Arrays.asList(HOST_A, HOST_B, HOST_C), result.get(0));
    }

    @Test
    public void testBalanceCpuLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        initMocks(policyUnit, hosts, vms);

        List<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertThat(result).hasSize(5);
        assertBalanceResult(VM_1, Arrays.asList(HOST_A, HOST_B, HOST_C), result.get(0));
        assertBalanceResult(VM_3, Arrays.asList(HOST_B, HOST_C), result.get(1));
        assertBalanceResult(VM_3, Arrays.asList(HOST_B, HOST_C), result.get(2));
        assertBalanceResult(VM_2, Arrays.asList(HOST_B, HOST_C), result.get(3));
        assertBalanceResult(VM_1, Arrays.asList(HOST_A, HOST_B, HOST_C), result.get(4));
    }

    @Test
    public void testBalanceCpuAndMemLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpumem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        initMocks(policyUnit, hosts, vms);

        List<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testBalanceMediumLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_medium_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        initMocks(policyUnit, hosts, vms);

        List<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testBalanceNoLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_no_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "768");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "128");

        initMocks(policyUnit, hosts, vms);

        List<BalanceResult> result = policyUnit.balance(cluster, new ArrayList<>(hosts.values()), parameters);
        assertFalse(result.isEmpty());

        assertThat(result).hasSize(4);
        assertBalanceResult(VM_2, Arrays.asList(HOST_A, HOST_B), result.get(0));
        assertBalanceResult(VM_1, Arrays.asList(HOST_A, HOST_B), result.get(1));
        assertBalanceResult(VM_2, Arrays.asList(HOST_A, HOST_B), result.get(2));
        assertBalanceResult(VM_1, Arrays.asList(HOST_A, HOST_B), result.get(3));
    }

    @Override
    protected void initMocks(CpuAndMemoryBalancingPolicyUnit unit,
            Map<Guid, VDS> hosts,
            Map<Guid, VM> vms) throws ParseException {

        super.initMocks(unit, hosts, vms);

        doReturn(new ArrayList<>(hosts.values())).when(vdsDao).getAllForCluster(any());

        // disable power management evaluation
        doReturn(null).when(policyUnit).evaluatePowerManagementSituation(any(), any(), any(), any(), any(), any());
    }
}
