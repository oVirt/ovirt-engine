package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class PowerSavingWeightPolicyUnitTest extends EvenDistributionWeightPolicyUnitTest {

    private static final Guid DESTINATION_HOST = new Guid("087fc690-de02-11e4-8830-0800200c9a66");

    private PowerSavingCPUWeightPolicyUnit powerSavingCPUWeightPolicyUnit;
    private PowerSavingMemoryWeightPolicyUnit powerSavingMemoryWeightPolicyUnit;

    @Before
    public void setUp() {
        PendingResourceManager pendingResourceManager = new PendingResourceManager();
        powerSavingCPUWeightPolicyUnit = new PowerSavingCPUWeightPolicyUnit(null, pendingResourceManager);
        powerSavingMemoryWeightPolicyUnit = new PowerSavingMemoryWeightPolicyUnit(null, pendingResourceManager);
    }

    @Test
    public void testScoreForCpuLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);
        testScore(powerSavingCPUWeightPolicyUnit, hosts, vms, DESTINATION_HOST);
    }

    @Test
    public void testScoreForMemoryLoad() throws Exception {
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);
        testScore(powerSavingMemoryWeightPolicyUnit, hosts, vms, DESTINATION_HOST);
    }

}
