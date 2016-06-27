package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class EvenDistributionWeightPolicyUnitTest extends AbstractPolicyUnitTest {

    private static final Guid DESTINATION_HOST = new Guid("087fc691-de02-11e4-8830-0800200c9a66");

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(
                    MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, 1000),
                    MockConfigRule.mockConfig(ConfigValues.VcpuConsumptionPercentage, 20),
                    MockConfigRule.mockConfig(ConfigValues.SpmVCpuConsumption, 1)
            );

    @Test
    public void testScoreForCpuLoad() throws Exception {
        EvenDistributionCPUWeightPolicyUnit unit = mockPolicyUnit(EvenDistributionCPUWeightPolicyUnit.class);
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);
        testScore(unit, hosts, vms, DESTINATION_HOST);
    }

    @Test
    public void testScoreForMemoryLoad() throws Exception {
        EvenDistributionMemoryWeightPolicyUnit unit = mockPolicyUnit(EvenDistributionMemoryWeightPolicyUnit.class);
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);
        testScore(unit, hosts, vms, DESTINATION_HOST);
    }

    protected <T extends EvenDistributionWeightPolicyUnit> void testScore(T unit,
            Map<Guid, VDS> hosts,
            Map<Guid, VM> vms,
            Guid destinationHost) {
        Cluster cluster = new Cluster();
        ArrayList<String> messages = new ArrayList<>();
        for (VM vm : vms.values()) {
            Guid hostId = selectedBestHost(unit, vm, new ArrayList<VDS>(hosts.values()));
            assertNotNull(hostId);
            assertEquals(destinationHost, hostId);
        }
    }

    protected <T extends EvenDistributionWeightPolicyUnit> T mockPolicyUnit(Class<T> unitType)
            throws Exception {
        return spy(unitType.getConstructor(PolicyUnit.class, PendingResourceManager.class)
                .newInstance(null, new PendingResourceManager()));
    }

    protected <T extends EvenDistributionWeightPolicyUnit> Guid selectedBestHost(T unit, VM vm, ArrayList<VDS> hosts) {
        List<Pair<Guid, Integer>> scores = unit.score(new Cluster(), hosts,
                vm,
                null);
        scores.sort((score1, score2) -> score1.getSecond().compareTo(score2.getSecond()));
        return scores.get(0).getFirst();
    }
}
