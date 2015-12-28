package org.ovirt.engine.core.bll.scheduling.arem;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

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

    private Cluster cluster;

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
        cluster = createCluster();
        host1 = createHost(cluster);
        host2 = createHost(cluster);
        host3 = createHost(cluster);
        vm1 = createVM(host1, Up, "vm1");
        vm2 = createVM(host1, Up, "vm2");
        vm3 = createVM(host1, Up, "vm3");
        vm4 = createVM(host2, Up, "vm4");
        vm5 = createVM(host3, Up, "vm5");
        vm6 = createVM(host3, Up, "vm6");
        prepareVmDao(vm1, vm2, vm3, vm4, vm5, vm6);

        when(affinityGroupDao.getAllAffinityGroupsByClusterId(any(Guid.class))).thenReturn(affinityGroups);

        when(schedulingManager.canSchedule(eq(cluster), any(VM.class), anyList(), anyList(),
                anyList(), anyList())).thenReturn(true);
    }

    @Test
    public void shouldNotTryToMigrateWhenNotSchedulable() {
        when(schedulingManager.canSchedule(eq(cluster), any(VM.class), anyList(), anyList(),
                anyList(), anyList())).thenReturn(false);
        affinityGroups.add(createAffinityGroup(cluster, true, vm1, vm2, vm4));
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isNull();
    }

    @Test
    public void shouldMigrateFromHostWithLessHosts() {
        AffinityGroup positiveGroup = createAffinityGroup(cluster, true, vm1, vm2, vm4);
        affinityGroups.add(positiveGroup);
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isEqualTo(vm4);
    }

    @Test
    public void shouldMigrateCandidateFromNegativeGroup() {
        AffinityGroup positiveSatisfiedGroup = createAffinityGroup(cluster, true, vm1, vm2);
        AffinityGroup negativeUnsatisfiedGroup = createAffinityGroup(cluster, false, vm2, vm3, vm6);
        affinityGroups.add(negativeUnsatisfiedGroup);
        affinityGroups.add(positiveSatisfiedGroup);
        final VM candidate = enforcer.chooseNextVmToMigrate(cluster);
        assertThat(candidate).isIn(vm2, vm3);
    }

    @Test
    public void shouldDoNothingWithoutGroups() {
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isNull();
    }

    @Test
    public void shouldDoNothingWhenSatisfied() {
        AffinityGroup positiveGroup = createAffinityGroup(cluster, true, vm1, vm2);
        AffinityGroup negativeGroup = createAffinityGroup(cluster, false, vm1, vm4);
        affinityGroups.add(positiveGroup);
        affinityGroups.add(negativeGroup);
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isNull();
    }

    @Test
    public void shouldMigrateMoreThanOneHost() {
        affinityGroups.add(createAffinityGroup(cluster, true, vm1, vm2, vm3, vm4, vm5, vm6));
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isEqualTo(vm4);
        vm4.setRunOnVds(host1.getId());
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isIn(vm5, vm6);
    }

    @Test
    public void shouldFixBiggerAffinityGroupFirst() {
        AffinityGroup bigGroup = createAffinityGroup(cluster, true, vm1, vm4, vm6);
        AffinityGroup smallGroup = createAffinityGroup(cluster, true, vm2, vm5);
        affinityGroups.add(bigGroup);
        affinityGroups.add(smallGroup);
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isIn(vm1, vm4, vm6);

        affinityGroups.clear();
        affinityGroups.add(smallGroup);
        affinityGroups.add(bigGroup);
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isIn(vm1, vm4, vm6);
    }

    @Test
    public void shouldFixEqualSizedAffinityGroupWithHigherIdFirst() {
        vm1.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000001"));
        vm4.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000007"));
        vm6.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000008"));
        vm2.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000003"));
        vm5.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000004"));
        prepareVmDao(vm1, vm2, vm4, vm5, vm6);

        final AffinityGroup lowIdGroup = createAffinityGroup(cluster, true, vm1, vm4);
        final AffinityGroup highIdGroup = createAffinityGroup(cluster, true, vm2, vm5);
        affinityGroups.add(lowIdGroup);
        affinityGroups.add(highIdGroup);
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isIn(vm2, vm5);

        affinityGroups.clear();
        affinityGroups.add(highIdGroup);
        affinityGroups.add(lowIdGroup);
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isIn(vm2, vm5);

        // Bigger groups should always come first
        affinityGroups.clear();
        final AffinityGroup biggestIdGroup = createAffinityGroup(cluster, true, vm1, vm4,  vm6);
        affinityGroups.add(highIdGroup);
        affinityGroups.add(biggestIdGroup);
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isIn(vm1, vm4, vm6);
    }

    @Test
    public void shouldSelectFirstSchedulableFromCandidatePool() {
        // Because three VMs are running on host1 and only two Vms (vm5 and vm6) are running on host3
        // the enforcer will detect vm5 and vm6 as possible candidates for migration
        affinityGroups.add(createAffinityGroup(cluster, true, vm1, vm2, vm3, vm5, vm6));

        // Say no to the first scheduling attempt and yes to the second one, to force the enforcer
        // to check every possible candidate
        when(schedulingManager.canSchedule(eq(cluster), any(VM.class), anyList(), anyList(),
                anyList(), anyList())).thenReturn(false, true);

        // There is no fixed order so we only know that one of those VMs will be selected for migration
        assertThat(enforcer.chooseNextVmToMigrate(cluster)).isIn(vm5, vm6);

        // Verify that the enforcer tried to schedule both candidate VMs.
        verify(schedulingManager).canSchedule(eq(cluster), eq(vm5), anyList(), anyList(),
                anyList(), anyList());
        verify(schedulingManager).canSchedule(eq(cluster), eq(vm6), anyList(), anyList(),
                anyList(), anyList());
    }

    private Cluster createCluster() {
        Guid id = Guid.newGuid();
        Cluster cluster = new Cluster();
        cluster.setClusterId(id);
        cluster.setId(id);
        cluster.setName("Default cluster");
        return cluster;
    }

    private VDS createHost(final Cluster cluster) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setClusterId(cluster.getId());
        return vds;
    }

    private VM createVM(final VDS host, VMStatus vmStatus, String name) {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterId(host.getClusterId());
        vm.setRunOnVds(host.getId());
        vm.setStatus(vmStatus);
        vm.setName(name);
        return vm;
    }

    private AffinityGroup createAffinityGroup(Cluster cluster, Boolean isPositive, final VM... vmList) {
        AffinityGroup ag = new AffinityGroup();
        ag.setId(Guid.newGuid());
        ag.setPositive(isPositive);
        ag.setClusterId(cluster.getId());
        ag.setEnforcing(true);
        ag.setEntityIds(Arrays.stream(vmList).map(VM::getId).collect(Collectors.toList()));
        return ag;
    }

    private void prepareVmDao(VM... vmList) {
        final List<VM> vms = Arrays.asList(vmList);
        doAnswer(new Answer() {
            @Override public Object answer(InvocationOnMock invocation) throws Throwable {
                final List<VM> selectedVms = new ArrayList<>();
                final Set<Guid> vmIds = new HashSet<>((List<Guid>) invocation.getArguments()[0]);
                for(VM vm :vms){
                    if(vmIds.contains(vm.getId())){
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
