package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class EvenDistributionWeightPolicyUnitTest extends AbstractPolicyUnitTest {

    private static final Guid DESTINATION_HOST = new Guid("087fc691-de02-11e4-8830-0800200c9a66");

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
    private EvenDistributionCPUWeightPolicyUnit evenDistributionCPUWeightPolicyUnit = new EvenDistributionCPUWeightPolicyUnit(null, new PendingResourceManager());

    @InjectMocks
    private EvenDistributionMemoryWeightPolicyUnit evenDistributionMemoryWeightPolicyUnit =  new EvenDistributionMemoryWeightPolicyUnit(null, new PendingResourceManager());

    @BeforeEach
    public void setUp() {
        when(vmOverheadCalculator.getTotalRequiredMemMb(any(VM.class))).thenAnswer(invocation -> invocation.<VM>getArgument(0).getMemSizeMb());
    }

    @Test
    public void testScoreForCpuLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);
        testScore(evenDistributionCPUWeightPolicyUnit, hosts, vms, DESTINATION_HOST);
    }

    @Test
    public void testScoreForMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_balancing_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_balancing_vms.csv", cache);
        testScore(evenDistributionMemoryWeightPolicyUnit, hosts, vms, DESTINATION_HOST);
    }

    protected <T extends PolicyUnitImpl> void testScore(T unit,
            Map<Guid, VDS> hosts,
            Map<Guid, VM> vms,
            Guid destinationHost) {
        for (VM vm : vms.values()) {
            Guid hostId = selectedBestHost(unit, vm, new ArrayList<VDS>(hosts.values()));
            assertNotNull(hostId);
            assertEquals(destinationHost, hostId);
        }
    }

    protected  <T extends PolicyUnitImpl> Guid selectedBestHost(T unit, VM vm, ArrayList<VDS> hosts) {
        List<Pair<Guid, Integer>> scores = unit.score(
                new SchedulingContext(new Cluster(), Collections.emptyMap()),
                hosts,
                Collections.singletonList(vm)
        );

        scores.sort(Comparator.comparing(Pair::getSecond));
        return scores.get(0).getFirst();
    }
}
