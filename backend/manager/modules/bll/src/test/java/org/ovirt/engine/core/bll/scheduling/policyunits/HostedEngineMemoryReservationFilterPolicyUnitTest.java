package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class HostedEngineMemoryReservationFilterPolicyUnitTest extends BaseCommandTest {
    private Guid clusterId;
    private List<VDS> hosts;
    private VM vm;
    private VM hostedEngine;

    private Map<String, String> parameters;
    private SchedulingContext context;
    private PerHostMessages messages;

    // Unit under test
    @InjectMocks
    private HostedEngineMemoryReservationFilterPolicyUnit policyUnit =
            new HostedEngineMemoryReservationFilterPolicyUnit(null, new PendingResourceManager());

    @Mock
    private VmDao vmDao;

    @BeforeEach
    public void setUp() {
        Cluster cluster = new Cluster();
        clusterId = Guid.newGuid();
        cluster.setId(clusterId);

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterId(clusterId);
        vm.setVmMemSizeMb(1024);

        hosts = new ArrayList<>();
        hosts.add(prepareHost("A", 8192, true, 2400, false));
        hosts.add(prepareHost("B", 8192, true, 2400, false));
        hosts.add(prepareHost("C", 8192, true, 2400, false));
        hosts.add(prepareHost("D", 8192, true, 2400, false));
        hosts.add(prepareHost("E", 8192, true, 2400, false));

        hostedEngine = new VM();
        hostedEngine.setOrigin(OriginType.HOSTED_ENGINE);
        hostedEngine.setVmMemSizeMb(4096);
        hostedEngine.setId(Guid.newGuid());
        hostedEngine.setClusterId(clusterId);
        hostedEngine.setRunOnVds(hosts.get(0).getId());

        parameters = new HashMap<>();
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "0");
        context = new SchedulingContext(cluster, parameters);

        messages = new PerHostMessages();

        doReturn(hostedEngine).when(vmDao).getHostedEngineVm();
    }

    @Test
    public void testNoHosts() {
        List<VDS> result = policyUnit.filter(context, new ArrayList<VDS>(), vm, messages);
        assertEquals(0, result.size());
    }

    @Test
    public void testWithNoRequiredSpares() {
        List<VDS> result = filter(vm);
        assertEquals(5, result.size());
    }

    @Test
    public void testWithEnoughSpares() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "5");
        List<VDS> result = filter(vm);
        assertEquals(5, result.size());
    }

    /**
     * Test a scenario where more spares than available hosts
     * is needed.
     *
     * All spares have enough memory to accomodate both the VM and the engine
     * and so all should be available for further evaluation.
     */
    @Test
    public void testWithoutEnoughSpares() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "6");
        List<VDS> result = filter(vm);
        assertEquals(5, result.size());
    }

    /**
     * Test a scenario where more spares than hosts with enough
     * memory to run HA + vm is needed.
     *
     * Only the host where the engine is currently running should be
     * a good enough host for another VM.
     */
    @Test
    public void testWithoutEnoughSparesFullMemory() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "5");
        hostedEngine.setVmMemSizeMb(7000);
        List<VDS> result = filter(vm);
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getName());
    }

    /**
     * Test a scenario where there is no hosted engine deployment, but the situation
     * would cause host removal under hosted engine cluster.
     *
     * All hosts should be returned.
     */
    @Test
    public void testNoHostedEngine() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "5");
        hostedEngine.setVmMemSizeMb(7000);
        hostedEngine.setOrigin(OriginType.OVIRT);
        doReturn(null).when(vmDao).getHostedEngineVm();

        List<VDS> result = filter(vm);
        assertEquals(5, result.size());
    }

    /**
     * Test a scenario where there is hosted engine deployment, the situation
     * would cause host removal under hosted engine cluster, but the HE VM
     * belongs to a different cluster.
     *
     * All hosts should be returned.
     */
    @Test
    public void testDifferentCluster() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "5");
        hostedEngine.setVmMemSizeMb(7000);
        hostedEngine.setClusterId(Guid.SYSTEM);
        List<VDS> result = filter(vm);
        assertEquals(5, result.size());
    }

    /**
     * Test a scenario where there are enough spares, but some hosts
     * do not have enough memory for the engine VM.
     *
     * 1 host currently hosts the HE - node 0
     * 2 hosts act as spares with enough memory for the vm - nodes 1 and 2
     * 2 hosts have enough memory for the vm, but not for HE = nodes 3 and 4
     *
     * Only one spare is required so all hosts can be used as candidates for
     * running the vm.
     *
     * All hosts should be returned.
     */
    @Test
    public void testWithEnoughSparesMemory() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "1");
        hostedEngine.setVmMemSizeMb(7000);

        hosts.get(3).setPhysicalMemMb(2048);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(vm);
        assertEquals(5, result.size());
    }

    /**
     * Test a scenario where there are the exact amount of spares, and some hosts
     * do not have enough memory for the engine VM.
     *
     * Three hosts (2 + the current host for HE VM) should be returned
     */
    @Test
    public void testExactSparesMemory() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "2");
        hostedEngine.setVmMemSizeMb(7000);

        hosts.get(3).setPhysicalMemMb(2048);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(vm);
        assertEquals(3, result.size());
    }

    /**
     * Test a scenario where there are not enough spares, some hosts
     * do not have enough memory for the engine VM.
     *
     * Three hosts (2 + the current host for HE VM) should be returned
     */
    @Test
    public void testWithoutEnoughSparesMemory() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "3");
        hostedEngine.setVmMemSizeMb(7000);

        hosts.get(3).setPhysicalMemMb(2048);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(vm);
        assertEquals(3, result.size());
    }

    /**
     * Test a scenario where there are not enough spares, some hosts
     * do not have enough memory for the engine VM and one host is not
     * HE enabled.
     *
     * Four hosts (3 + the current host for HE VM) should be returned
     */
    @Test
    public void testWithNonHEHost() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "3");
        hostedEngine.setVmMemSizeMb(7000);

        hosts.get(2).setHighlyAvailableIsActive(false);
        hosts.get(3).setPhysicalMemMb(2048);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(vm);
        assertEquals(4, result.size());
    }

    /**
     * Test a scenario where there are not enough spares, some hosts
     * do not have enough memory for the engine VM and one host is not
     * ready and has HE score 0.
     *
     * Four hosts (3 + the current host for HE VM) should be returned
     */
    @Test
    public void testWith0ScoreHost() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "3");
        hostedEngine.setVmMemSizeMb(7000);

        hosts.get(2).setHighlyAvailableScore(0);
        hosts.get(3).setPhysicalMemMb(2048);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(vm);
        assertEquals(4, result.size());
    }

    /**
     * Test a scenario where there are not enough spares, some hosts
     * do not have enough memory for the engine VM and one host is not
     * ready and has HE score 0.
     *
     * The spare host is capable of running HE and VM together though.
     *
     * All hosts should be returned
     */
    @Test
    public void testWith0ScoreHostAndEnoughMemoryForTwo() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "3");
        hostedEngine.setVmMemSizeMb(5000);

        hosts.get(2).setHighlyAvailableScore(0);
        hosts.get(3).setPhysicalMemMb(2048);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(vm);
        assertEquals(5, result.size());
    }

    /**
     * Test a scenario where there are not enough spares, some hosts
     * do not have enough memory for the engine VM and one host is not
     * ready and has HE score 0.
     *
     * One spare host is capable of running HE and VM together and the second
     * one is not.
     *
     * Four hosts should be returned (one spare does not have enough mem
     * for the additional VM).
     */
    @Test
    public void testWith0ScoreHostAndSomeHaveEnoughMemoryForTwo() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "3");
        hostedEngine.setVmMemSizeMb(5000);

        hosts.get(2).setHighlyAvailableScore(0);
        hosts.get(3).setPhysicalMemMb(6000);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(vm);
        assertEquals(4, result.size());
    }

    /**
     * Test a scenario where there are not enough spares, some hosts
     * do not have enough memory for the engine VM and one host is
     * in local maintenance.
     *
     * One spare host is capable of running HE and VM together and the second
     * one is not.
     *
     * Four hosts should be returned (one spare does not have enough mem
     * for the additional VM).
     */
    @Test
    public void testWithMaintenancedHostAndSomeHaveEnoughMemoryForTwo() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "3");
        hostedEngine.setVmMemSizeMb(5000);

        hosts.get(2).setHighlyAvailableLocalMaintenance(true);
        hosts.get(3).setPhysicalMemMb(6000);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(vm);
        assertEquals(4, result.size());
    }

    /**
     * Test a scenario when engine VM is scheduled and there are not enough spares,
     * some hosts do not have enough memory for the engine VM and one host is
     * in local maintenance.
     *
     * One spare host is capable of running HE and VM together and the second
     * one is not.
     *
     * All hosts should be returned.
     */
    @Test
    public void testStartingTheEngineWithNotEnoughSpares() {
        parameters.put(PolicyUnitParameter.HE_SPARES_COUNT.getDbName(), "3");
        hostedEngine.setVmMemSizeMb(5000);

        hosts.get(2).setHighlyAvailableLocalMaintenance(true);
        hosts.get(3).setPhysicalMemMb(7000);
        hosts.get(4).setPhysicalMemMb(2048);

        List<VDS> result = filter(hostedEngine);
        assertEquals(5, result.size());
    }

    /**
     * Test a scenario where one host was placed to local maintenance.
     * It should not affect the result with regards to scheduling
     * arbitrary VMs.
     */
    @Test
    public void testHostInMaintenance() {
        hosts.get(0).setHighlyAvailableLocalMaintenance(true);
        List<VDS> result = filter(vm);
        assertEquals(5, result.size());
    }

    private List<VDS> filter(VM vm) {
        return policyUnit.filter(context, hosts, Collections.singletonList(vm), messages);
    }

    private VDS prepareHost(String name, int freeMemoryMb, boolean heEnabled, int heScore, boolean localMaintnance) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setClusterId(clusterId);
        host.setVdsName(name);
        host.setPhysicalMemMb(freeMemoryMb);
        host.setMemCommited(0);
        host.setMemShared(0L);
        host.setReservedMem(128);
        host.setGuestOverhead(64);
        host.setHighlyAvailableIsActive(heEnabled);
        host.setHighlyAvailableScore(heScore);
        host.setHighlyAvailableLocalMaintenance(localMaintnance);
        host.setMaxVdsMemoryOverCommit(100);
        return host;
    }
}
