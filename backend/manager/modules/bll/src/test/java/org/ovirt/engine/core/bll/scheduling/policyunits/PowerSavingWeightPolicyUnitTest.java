package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class PowerSavingWeightPolicyUnitTest extends EvenDistributionWeightPolicyUnitTest {

    private static final Guid DESTINATION_HOST = new Guid("087fc691-de02-11e4-8830-0800200c9a66");

    private PowerSavingCPUWeightPolicyUnit powerSavingCPUWeightPolicyUnit;
    private PowerSavingMemoryWeightPolicyUnit powerSavingMemoryWeightPolicyUnit;
    private Map<String, String> parameters = new HashMap<>();

    @BeforeEach
    public void setUp() {
        PendingResourceManager pendingResourceManager = new PendingResourceManager();
        powerSavingCPUWeightPolicyUnit = new PowerSavingCPUWeightPolicyUnit(null, pendingResourceManager);
        powerSavingMemoryWeightPolicyUnit = new PowerSavingMemoryWeightPolicyUnit(null, pendingResourceManager);

        parameters.put(PolicyUnitParameter.HIGH_UTILIZATION.getDbName(), "80");
        parameters.put(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName(), "700");
        parameters.put(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName(), "900");
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

    @Override
    protected <T extends PolicyUnitImpl> Guid selectedBestHost(T unit, VM vm, ArrayList<VDS> hosts) {
        return unit.score(new Cluster(), hosts, vm, parameters).stream()
                .min(Comparator.comparing(Pair::getSecond))
                .map(Pair::getFirst)
                .orElse(null);
    }
}
