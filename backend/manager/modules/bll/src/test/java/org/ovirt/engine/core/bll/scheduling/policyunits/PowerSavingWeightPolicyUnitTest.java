package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class PowerSavingWeightPolicyUnitTest extends EvenDistributionWeightPolicyUnitTest {

    private static final Guid DESTINATION_HOST = new Guid("087fc690-de02-11e4-8830-0800200c9a66");

    @Test
    public void testScoreForCpuLoad() throws Exception {
        PowerSavingCPUWeightPolicyUnit unit = mockPolicyUnit(PowerSavingCPUWeightPolicyUnit.class);
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_cpu_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);
        testScore(unit, hosts, vms, DESTINATION_HOST);
    }

    @Test
    public void testScoreForMemoryLoad() throws Exception {
        PowerSavingMemoryWeightPolicyUnit unit = mockPolicyUnit(PowerSavingMemoryWeightPolicyUnit.class);
        Map<Guid, BusinessEntity<Guid>> cache = newCache();
        final Map<Guid, VDS> hosts = loadHosts("basic_power_saving_hosts_mem_load.csv", cache);
        final Map<Guid, VM> vms = loadVMs("basic_power_saving_vms.csv", cache);
        testScore(unit, hosts, vms, DESTINATION_HOST);
    }

}
