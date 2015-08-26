package org.ovirt.engine.core.bll.scheduling.arem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.businessentities.VMStatus.Up;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

@RunWith(MockitoJUnitRunner.class)
public class AffinityRulesEnforcerTest {

    @Mock
    private AffinityGroupDao affinityGroupDao;
    @Mock
    private SchedulingManager schedulingManager;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private VmDao vmDao;

    private VDSGroup vdsGroup;

    private VDS host1;

    private VDS host2;

    private VDS host3;

    private VM vm1;

    private VM vm2;

    private VM vm3;

    private VM vm4;

    private VM vm5;

    private VM vm6;

    private final List<AffinityGroup> affinityGroups = new ArrayList<>();

    @InjectMocks
    private AffinityRulesEnforcer enforcer;

    /**
     * Setup a basic test scenario consisting of one cluster with three hosts and a bunch of virtual machines:
     * - host1 runs vm1, vm2 and vm3
     * - host2 runs vm4
     * - host3 runs vm5 and vm6
     */
    @Before
    public void setup() {
        affinityGroups.clear();
        vdsGroup = createVdsGroup();
        host1 = createHost(vdsGroup);
        host2 = createHost(vdsGroup);
        host3 = createHost(vdsGroup);
        vm1 = createVM(host1, Up, "vm1");
        vm2 = createVM(host1, Up, "vm2");
        vm3 = createVM(host1, Up, "vm3");
        vm4 = createVM(host2, Up, "vm4");
        vm5 = createVM(host3, Up, "vm5");
        vm6 = createVM(host3, Up, "vm6");

        prepareVmDao(vm1, vm2, vm3, vm4, vm5, vm6);
        when(affinityGroupDao.getAllAffinityGroupsByClusterId(any(Guid.class))).thenReturn(affinityGroups);

        when(schedulingManager.canSchedule(eq(vdsGroup), any(VM.class), anyList(), anyList(),
                anyList(), anyList())).thenReturn(true);
    }

    @Test
    public void shouldNotTryToMigrateWhenNotSchedulable() {
        when(schedulingManager.canSchedule(eq(vdsGroup), any(VM.class), anyList(), anyList(),
                anyList(), anyList())).thenReturn(false);
        affinityGroups.add(createAffinityGroup(vdsGroup, true, vm1, vm2, vm4));
        assertNull(enforcer.chooseNextVmToMigrate(vdsGroup));
    }

    @Test
    public void shouldMigrateFromHostWithLessHosts() {
        AffinityGroup positiveGroup = createAffinityGroup(vdsGroup, true, vm1, vm2, vm4);
        affinityGroups.add(positiveGroup);
        assertEquals(vm4, enforcer.chooseNextVmToMigrate(vdsGroup));
    }

    @Test
    public void shouldMigrateCandidateFromNegativeGroup() {
        AffinityGroup positiveSatisfiedGroup = createAffinityGroup(vdsGroup, true, vm1, vm2);
        AffinityGroup negativeUnsatisfiedGroup = createAffinityGroup(vdsGroup, false, vm2, vm3, vm6);
        affinityGroups.add(negativeUnsatisfiedGroup);
        affinityGroups.add(positiveSatisfiedGroup);
        final VM candidate = enforcer.chooseNextVmToMigrate(vdsGroup);
        assertTrue(Arrays.asList(vm2, vm3).contains(candidate));
    }

    @Test
    public void shouldDoNothingWithoutGroups() {
        assertNull(enforcer.chooseNextVmToMigrate(vdsGroup));
    }

    @Test
    public void shouldDoNothingWhenSatisfied() {
        AffinityGroup positiveGroup = createAffinityGroup(vdsGroup, true, vm1, vm2);
        AffinityGroup negativeGroup = createAffinityGroup(vdsGroup, false, vm1, vm4);
        affinityGroups.add(positiveGroup);
        affinityGroups.add(negativeGroup);
        assertNull(enforcer.chooseNextVmToMigrate(vdsGroup));
    }

    @Test
    public void shouldMigrateMoreThanOneHost() {
        affinityGroups.add(createAffinityGroup(vdsGroup, true, vm1, vm2, vm3, vm4, vm5, vm6));
        assertEquals(vm4, enforcer.chooseNextVmToMigrate(vdsGroup));
        vm4.setRunOnVds(host1.getId());
        assertTrue(Arrays.asList(vm5, vm6).contains(enforcer.chooseNextVmToMigrate(vdsGroup)));
    }

    @Test
    public void shouldSelectFirstSchedulableFromCandidatePool() {
        // Because three VMs are running on host1 and only two Vms (vm5 and vm6) are running on host3
        // the enforcer will detect vm5 and vm6 as possible candidates for migration
        affinityGroups.add(createAffinityGroup(vdsGroup, true, vm1, vm2, vm3, vm5, vm6));

        // Say no to the first scheduling attempt and yes to the second one, to force the enforcer
        // to check every possible candidate
        when(schedulingManager.canSchedule(eq(vdsGroup), any(VM.class), anyList(), anyList(),
                anyList(), anyList())).thenReturn(false, true);

        // There is no fixed order so we only know that one of those VMs will be selected for migration
        assertTrue(Arrays.asList(vm5, vm6).contains(enforcer.chooseNextVmToMigrate(vdsGroup)));

        // Verify that the enforcer tried to schedule both candidate VMs.
        verify(schedulingManager).canSchedule(eq(vdsGroup), eq(vm5), anyList(), anyList(),
                anyList(), anyList());
        verify(schedulingManager).canSchedule(eq(vdsGroup), eq(vm6), anyList(), anyList(),
                anyList(), anyList());
    }

    private VDSGroup createVdsGroup() {
        Guid id = Guid.newGuid();
        VDSGroup cluster = new VDSGroup();
        cluster.setVdsGroupId(id);
        cluster.setId(id);
        cluster.setName("Default cluster");
        return cluster;
    }

    private VDS createHost(final VDSGroup vdsGroup) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsGroupId(vdsGroup.getId());
        return vds;
    }

    private VM createVM(final VDS host, VMStatus vmStatus, String name) {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setVdsGroupId(host.getVdsGroupId());
        vm.setRunOnVds(host.getId());
        vm.setStatus(vmStatus);
        vm.setName(name);
        return vm;
    }

    private AffinityGroup createAffinityGroup(VDSGroup vdsGroup, Boolean isPositive, final VM... vmList) {
        AffinityGroup ag = new AffinityGroup();
        ag.setId(Guid.newGuid());
        ag.setPositive(isPositive);
        ag.setClusterId(vdsGroup.getId());
        ag.setEnforcing(true);
        ag.setEntityIds(LinqUtils.transformToList(Arrays.asList(vmList), new Function<VM, Guid>() {
            @Override public Guid eval(VM vm) {
                return vm.getId();
            }
        }));
        return ag;
    }

    private void prepareVmDao(VM... vmList) {
        final List<VM> vms = Arrays.asList(vmList);
        doAnswer(new Answer() {
            @Override public Object answer(InvocationOnMock invocation) throws Throwable {
                final List<VM> selectedVms = new ArrayList<>();
                final Set<Guid> vmIds = new HashSet<>((List<Guid>) invocation.getArguments()[0]);
                for (VM vm : vms) {
                    if (vmIds.contains(vm.getId())) {
                        selectedVms.add(vm);
                    }
                }
                return selectedVms;
            }
        }).when(vmDao).getVmsByIds(anyList());
        for (VM vm : vmList) {
            when(vmDao.get(eq(vm.getId()))).thenReturn(vm);
        }
    }
}
