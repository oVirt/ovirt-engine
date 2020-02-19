package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({ MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class PowerSavingWeightPolicyUnitTest extends AbstractPolicyUnitTest {

    private static final Guid HOST_A = new Guid("087fc690-de02-11e4-8830-0800200c9a66");
    private static final Guid HOST_B = new Guid("087fc691-de02-11e4-8830-0800200c9a66");
    private static final Guid HOST_C = new Guid("087fc692-de02-11e4-8830-0800200c9a66");

    private static final Guid VM_1 = new Guid("087fc692-de02-11e4-8830-0800200c9a66");
    private static final Guid VM_2 = new Guid("087fc693-de02-11e4-8830-0800200c9a66");
    private static final Guid VM_3 = new Guid("087fc694-de02-11e4-8830-0800200c9a66");

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxSchedulerWeight, 1000),
                MockConfigDescriptor.of(ConfigValues.VcpuConsumptionPercentage, 10),
                MockConfigDescriptor.of(ConfigValues.SpmVCpuConsumption, 1)
        );
    }

    @Mock
    private VmOverheadCalculator vmOverheadCalculator;

    @InjectMocks
    private PowerSavingCPUWeightPolicyUnit powerSavingCPUWeightPolicyUnit =  new PowerSavingCPUWeightPolicyUnit(null, new PendingResourceManager());

    @InjectMocks
    private PowerSavingMemoryWeightPolicyUnit powerSavingMemoryWeightPolicyUnit = new PowerSavingMemoryWeightPolicyUnit(null, new PendingResourceManager());

    private Map<String, String> parameters = new HashMap<>();

    @BeforeEach
    public void setUp() {
        when(vmOverheadCalculator.getTotalRequiredMemMb(any(VM.class))).thenAnswer(invocation -> invocation.<VM>getArgument(0).getMemSizeMb());

        parameters.put(PolicyUnitParameter.HIGH_UTILIZATION.getDbName(), "80");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "600");
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
    }

    @Test
    public void testScoreForCpuLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        testScore(powerSavingCPUWeightPolicyUnit, hosts, vms.get(VM_1), HOST_B);
        testScore(powerSavingCPUWeightPolicyUnit, hosts, vms.get(VM_2), HOST_C);
        testScore(powerSavingCPUWeightPolicyUnit, hosts, vms.get(VM_3), HOST_B);
    }

    @Test
    public void testScoreForMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);

        testScore(powerSavingMemoryWeightPolicyUnit, hosts, vms.get(VM_1), HOST_B);
        testScore(powerSavingMemoryWeightPolicyUnit, hosts, vms.get(VM_2), HOST_C);
        testScore(powerSavingMemoryWeightPolicyUnit, hosts, vms.get(VM_3), HOST_B);
    }

    private <T extends PolicyUnitImpl> void testScore(T unit,
            Map<Guid, VDS> hosts,
            VM vm,
            Guid destinationHost) {
        Guid hostId = selectedBestHost(unit, vm, new ArrayList<>(hosts.values()));
        assertNotNull(hostId);
        assertEquals(destinationHost, hostId);
    }

    private <T extends PolicyUnitImpl> Guid selectedBestHost(T unit, VM vm, ArrayList<VDS> hosts) {
        return unit.score(new SchedulingContext(new Cluster(), parameters),
                hosts,
                Collections.singletonList(vm)).stream()
                .min(Comparator.comparing(Pair::getSecond))
                .map(Pair::getFirst)
                .orElse(null);
    }
}
