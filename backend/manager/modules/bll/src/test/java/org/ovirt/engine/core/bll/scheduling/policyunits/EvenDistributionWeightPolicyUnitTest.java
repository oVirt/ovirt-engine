package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class EvenDistributionWeightPolicyUnitTest extends AbstractPolicyUnitTest {

    private static final Guid DESTINATION_HOST = new Guid("087fc691-de02-11e4-8830-0800200c9a66");

    private EvenDistributionCPUWeightPolicyUnit evenDistributionCPUWeightPolicyUnit;
    private EvenDistributionMemoryWeightPolicyUnit evenDistributionMemoryWeightPolicyUnit;

    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule(
        mockConfig(ConfigValues.MaxSchedulerWeight, 1000),
        mockConfig(ConfigValues.VcpuConsumptionPercentage, 10),
        mockConfig(ConfigValues.SpmVCpuConsumption, 1)
    );

    @Before
    public void setUp() {
        PendingResourceManager pendingResourceManager = new PendingResourceManager();
        evenDistributionCPUWeightPolicyUnit = new EvenDistributionCPUWeightPolicyUnit(null, pendingResourceManager);
        evenDistributionMemoryWeightPolicyUnit = new EvenDistributionMemoryWeightPolicyUnit(null, pendingResourceManager);
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
        List<Pair<Guid, Integer>> scores = unit.score(new Cluster(), hosts, vm, null);
        scores.sort(Comparator.comparing(Pair::getSecond));
        return scores.get(0).getFirst();
    }
}
