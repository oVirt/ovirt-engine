package org.ovirt.engine.core.bll.scheduling.arem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.businessentities.VMStatus.Up;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AffinityRulesEnforcerTest {

    @Mock
    private AffinityGroupDao affinityGroupDao;
    @Mock
    private LabelDao labelDao;
    @Mock
    private SchedulingManager schedulingManager;
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

    private final List<Label> labels = new ArrayList<>();

    private Map<Guid, List<VDS>> possibleHosts;

    @InjectMocks
    private AffinityRulesEnforcer enforcer;

    /**
     * Setup a basic test scenario consisting of one cluster with three hosts and a bunch of virtual machines:
     * - host1 runs vm1, vm2 and vm3
     * - host2 runs vm4
     * - host3 runs vm5 and vm6
     */
    @BeforeEach
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

        possibleHosts = Stream.of(vm1, vm2, vm3, vm4, vm5, vm6)
                .collect(Collectors.toMap(VM::getId, vm -> Arrays.asList(host1, host2, host3)));

        when(affinityGroupDao.getAllAffinityGroupsWithFlatLabelsByClusterId(any())).thenAnswer(invocation -> copyGroups());
        when(labelDao.getAllByClusterId(any())).thenReturn(labels);

        when(schedulingManager.canSchedule(eq(cluster), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(possibleHosts);
        when(schedulingManager.prepareCall(eq(cluster))).thenCallRealMethod();
    }

    @Test
    public void shouldNotTryToMigrateWhenNotSchedulable() {
        possibleHosts.clear();

        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2, vm4));
        assertThat(getVmsToMigrate()).isEmpty();
        affinityGroups.clear();
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule.POSITIVE, true,
                Arrays.asList(host2, host3), vm1));
        assertThat(getVmsToMigrate()).isEmpty();
    }

    @Test
    public void shouldFirstMigrateFromHostWithLessVms() {
        AffinityGroup positiveGroup =
                createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2, vm4);
        affinityGroups.add(positiveGroup);

        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm4),
                Arrays.asList(vm1, vm2)
        ));
    }

    @Test
    public void shouldMigrateCandidateFromNegativeGroup() {
        AffinityGroup positiveSatisfiedGroup =
                createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2);
        AffinityGroup negativeUnsatisfiedGroup = createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE,
                vm2, vm3, vm6);
        affinityGroups.add(negativeUnsatisfiedGroup);
        affinityGroups.add(positiveSatisfiedGroup);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm2, vm3)
        ));

        positiveSatisfiedGroup =
                createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule.POSITIVE, true,
                        Arrays.asList(host1, host2), vm1, vm2, vm3);

        negativeUnsatisfiedGroup = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .NEGATIVE, true,
                Arrays.asList(host1, host3), vm5, vm6);

        affinityGroups.clear();
        affinityGroups.add(negativeUnsatisfiedGroup);
        affinityGroups.add(positiveSatisfiedGroup);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm5, vm6)
        ));
    }

    @Test
    public void shouldDoNothingWithoutGroups() {
        assertThat(getVmsToMigrate()).isEmpty();
    }

    @Test
    public void shouldDoNothingWhenSatisfied() {
        AffinityGroup positiveGroup = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2);
        AffinityGroup negativeGroup = createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, vm1, vm4);
        affinityGroups.add(positiveGroup);
        affinityGroups.add(negativeGroup);
        assertThat(getVmsToMigrate()).isEmpty();

        positiveGroup = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule.POSITIVE, true,
                Arrays.asList(host1), vm1, vm2, vm3);
        negativeGroup = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule.NEGATIVE, true,
                Arrays.asList(host1), vm4);

        affinityGroups.clear();
        affinityGroups.add(positiveGroup);
        affinityGroups.add(negativeGroup);
        assertThat(getVmsToMigrate()).isEmpty();
    }

    @Test
    public void shouldMigrateMoreThanOneHost() {
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2, vm3, vm4, vm5, vm6));
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm4),
                Arrays.asList(vm5, vm6),
                Arrays.asList(vm1, vm2, vm3)
        ));

        vm4.setRunOnVds(host1.getId());
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm5, vm6),
                Arrays.asList(vm1, vm2, vm3, vm4)
        ));
    }

    @Test
    public void shouldFixBiggerAffinityGroupFirst() {
        AffinityGroup bigGroup = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm4, vm6);
        AffinityGroup smallGroup = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm2, vm5);
        affinityGroups.add(bigGroup);
        affinityGroups.add(smallGroup);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm1, vm4, vm6),
                Arrays.asList(vm2, vm5)
        ));

        affinityGroups.clear();
        affinityGroups.add(smallGroup);
        affinityGroups.add(bigGroup);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm1, vm4, vm6),
                Arrays.asList(vm2, vm5)
        ));
    }

    @Test
    public void shouldFixVmWithMostViolationsFirst() {
        AffinityGroup groupA = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule.POSITIVE,
                true, Arrays.asList(host2), vm1, vm2);
        AffinityGroup groupB = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule.POSITIVE,
                true, Arrays.asList(host3), vm1, vm2);
        AffinityGroup groupC = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule.POSITIVE,
                true, Arrays.asList(host1), vm4);

        affinityGroups.clear();
        affinityGroups.add(groupA);
        affinityGroups.add(groupB);
        affinityGroups.add(groupC);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm4),
                Arrays.asList(vm1, vm2)
        ));

        affinityGroups.clear();
        affinityGroups.add(groupB);
        affinityGroups.add(groupC);
        affinityGroups.add(groupA);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm4),
                Arrays.asList(vm1, vm2)
        ));
    }

    @Test
    public void shouldFixEqualSizedAffinityGroupWithHigherIdFirst() {
        vm1.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000001"));
        vm4.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000007"));
        vm6.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000008"));
        vm2.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000003"));
        vm5.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000004"));
        prepareVmDao(vm1, vm2, vm4, vm5, vm6);

        possibleHosts.clear();
        Stream.of(vm1, vm2, vm3, vm4, vm5, vm6)
                .forEach(vm -> possibleHosts.put(vm.getId(), Arrays.asList(host1, host2, host3)));

        final AffinityGroup lowIdGroup =
                createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm4);
        final AffinityGroup highIdGroup =
                createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm2, vm5);
        affinityGroups.add(lowIdGroup);
        affinityGroups.add(highIdGroup);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm2, vm5),
                Arrays.asList(vm1, vm4)
        ));

        affinityGroups.clear();
        affinityGroups.add(highIdGroup);
        affinityGroups.add(lowIdGroup);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm2, vm5),
                Arrays.asList(vm1, vm4)
        ));

        // Bigger groups should always come first
        affinityGroups.clear();
        final AffinityGroup biggestIdGroup =
                createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm4, vm6);
        affinityGroups.add(highIdGroup);
        affinityGroups.add(biggestIdGroup);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm1, vm4, vm6),
                Arrays.asList(vm2, vm5)
        ));
    }

    @Test
    public void shouldSelectFirstSchedulableFromCandidatePool() {
        // Because three VMs are running on host1 and only two Vms (vm5 and vm6) are running on host3
        // the enforcer will detect vm5 and vm6 as possible candidates for migration
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2, vm3,
                vm5, vm6));

        // Say no when scheduling vm6
        possibleHosts.put(vm6.getId(), Collections.emptyList());

        // There is no fixed order so we only know that one of those VMs will be selected for migration
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm5),
                Arrays.asList(vm1, vm2, vm3)
        ));
    }

    @Test
    public void shouldTryVMsFromAllHosts() {
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm5, vm6));

        // vm1 cannot be scheduled
        possibleHosts.put(vm1.getId(), Collections.emptyList());

        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm5, vm6)
        ));

        affinityGroups.clear();
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2, vm3, vm4, vm5, vm6));

        // cannot schedule vm4, vm5 and vm6
        possibleHosts.put(vm4.getId(), Collections.emptyList());
        possibleHosts.put(vm5.getId(), Collections.emptyList());
        possibleHosts.put(vm6.getId(), Collections.emptyList());

        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm2, vm3)
        ));
    }

    @Test
    /**
     * Test conflicts for vm to host affinity including combinations
     * with vm to vm affinity.
     *
     * The following scenarios are tested:
     * - Hosts with positive and negative affinity to vm:
     *   {vm1 + host1} , {vm1 - host1}     enforcing
     *   {vm1 [+] host1} , {vm1 - host1}   non enforcing/enforcing
     *
     * - Hosts with positive vm to vm conflict:
     *   {vm1 + host1},{vm1+vm2},{vm2 - host1}
     *
     * - Hosts with negative vm to vm conflict:
     *   {vm1 + host1},{vm1 - vm2},{vm2 + host1}
     *
     * - Non intersecting positive hosts conflict:
     *   {vm1 + host1,host2} , {vm1 + host1,host3}
     *
     * (  + is enforcing positive affinity)
     * ( [+] is non enforcing positive affinity)
     * (  - is enforcing negative affinity)
     * ( [-] is non enforcing negative affinity)
     * ( {} is an affinity group)
     *
     */
    public void testVmToHostsExpectedConflictingAffinityGroupsConflicts() {

        //{vm1 + host1}
        AffinityGroup groupA = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .POSITIVE, true,
                Arrays.asList(host1), vm1);
        //{vm1 - host1}
        AffinityGroup groupB = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .NEGATIVE, true,
                Arrays.asList(host1), vm1);

        affinityGroups.add(groupA);
        affinityGroups.add(groupB);

        Set<AffinityGroup> expectedConflictingAffinityGroups = new HashSet<>();
        expectedConflictingAffinityGroups.addAll(Arrays.asList(groupA, groupB));

        AffinityRulesUtils.AffinityGroupConflicts conflicts =
                AffinityRulesUtils.checkForAffinityGroupHostsConflict(affinityGroups).get(0);

        assertThat(conflicts.getType())
                .isEqualTo(AffinityRulesConflicts.VM_TO_HOST_CONFLICT_IN_ENFORCING_POSITIVE_AND_NEGATIVE_AFFINITY);

        assertThat(conflicts.getAffinityGroups())
                .isEqualTo(expectedConflictingAffinityGroups);

        assertThat(conflicts.getHosts())
                .isEqualTo(new HashSet<>(Arrays.asList(host1.getId())));

        assertThat(conflicts.getVms())
                .isEqualTo(new HashSet<>(Arrays.asList(vm1.getId())));

        //{vm1 [+] host1}
        groupA = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .POSITIVE, false,
                Arrays.asList(host1), vm1);
        //{vm1 - host1}
        groupB = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .NEGATIVE, true,
                Arrays.asList(host1), vm1);

        affinityGroups.clear();
        affinityGroups.add(groupA);
        affinityGroups.add(groupB);

        expectedConflictingAffinityGroups.clear();
        expectedConflictingAffinityGroups.addAll(Arrays.asList(groupA, groupB));

        conflicts = AffinityRulesUtils
                .checkForAffinityGroupHostsConflict(affinityGroups).get(0);

        assertThat(conflicts.getType())
                .isEqualTo(AffinityRulesConflicts.VM_TO_HOST_CONFLICT_IN_POSITIVE_AND_NEGATIVE_AFFINITY);

        assertThat(conflicts.getAffinityGroups())
                .isEqualTo(expectedConflictingAffinityGroups);

        assertThat(conflicts.getHosts())
                .isEqualTo(new HashSet<>(Arrays.asList(host1.getId())));

        assertThat(conflicts.getVms())
                .isEqualTo(new HashSet<>(Arrays.asList(vm1.getId())));

        //{vm1 + host1}
        groupA = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .POSITIVE, true,
                Arrays.asList(host1), vm1);
        //{vm2 - host1}
        groupB = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .NEGATIVE, true,
                Arrays.asList(host1), vm2);
        //{vm1+vm2}
        AffinityGroup groupC = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2);

        affinityGroups.clear();
        affinityGroups.add(groupA);
        affinityGroups.add(groupB);
        affinityGroups.add(groupC);

        expectedConflictingAffinityGroups.clear();
        expectedConflictingAffinityGroups.addAll(Arrays.asList(groupA, groupB));

        conflicts = AffinityRulesUtils
                .checkForAffinityGroupHostsConflict(affinityGroups).get(0);

        assertThat(conflicts.getType())
                .isEqualTo(AffinityRulesConflicts.VM_TO_HOST_CONFLICTS_POSITIVE_VM_TO_VM_AFFINITY);

        assertThat(conflicts.getAffinityGroups())
                .isEqualTo(expectedConflictingAffinityGroups);

        assertThat(conflicts.getHosts())
                .isEqualTo(new HashSet<>(Arrays.asList(host1.getId())));

        assertThat(conflicts.getVms())
                .isEqualTo(new HashSet<>(Arrays.asList(vm1.getId(), vm2.getId())));

        //{vm1 + host1}
        groupA = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .POSITIVE, true,
                Arrays.asList(host1), vm1);
        //{vm2 + host1}
        groupB = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .POSITIVE, true,
                Arrays.asList(host1), vm2);
        //{vm1 - vm2}
        groupC = createAffinityGroup(cluster, EntityAffinityRule.NEGATIVE, vm1, vm2);

        affinityGroups.clear();
        affinityGroups.add(groupA);
        affinityGroups.add(groupB);
        affinityGroups.add(groupC);

        expectedConflictingAffinityGroups.clear();
        expectedConflictingAffinityGroups.addAll(Arrays.asList(groupA, groupB, groupC));

        conflicts = AffinityRulesUtils
                .checkForAffinityGroupHostsConflict(affinityGroups).get(0);

        assertThat(conflicts.getType())
                .isEqualTo(AffinityRulesConflicts.VM_TO_HOST_CONFLICTS_NEGATIVE_VM_TO_VM_AFFINITY);

        assertThat(conflicts.getAffinityGroups())
                .isEqualTo(expectedConflictingAffinityGroups);

        assertThat(conflicts.getHosts())
                .isEqualTo(new HashSet<>(Arrays.asList(host1.getId())));

        assertThat(conflicts.getVms())
                .isEqualTo(new HashSet<>(Arrays.asList(vm1.getId(), vm2.getId())));

        //{vm1 + host1,host2}
        groupA = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .POSITIVE, true,
                Arrays.asList(host1, host2), vm1);
        //{vm1 + host1,host3}
        groupB = createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, EntityAffinityRule
                        .POSITIVE, true,
                Arrays.asList(host1, host3), vm1);

        affinityGroups.clear();
        affinityGroups.add(groupA);
        affinityGroups.add(groupB);

        expectedConflictingAffinityGroups.clear();
        expectedConflictingAffinityGroups.addAll(Arrays.asList(groupA, groupB));

        conflicts = AffinityRulesUtils
                .checkForAffinityGroupHostsConflict(affinityGroups).get(0);

        assertThat(conflicts.getType())
                .isEqualTo(AffinityRulesConflicts.NON_INTERSECTING_POSITIVE_HOSTS_AFFINITY_CONFLICTS);

        assertThat(conflicts.getAffinityGroups())
                .isEqualTo(expectedConflictingAffinityGroups);

        assertThat(conflicts.getHosts())
                .isEqualTo(new HashSet<>(Arrays.asList(host2.getId(), host3.getId())));

        assertThat(conflicts.getVms())
                .isEqualTo(new HashSet<>(Arrays.asList(vm1.getId())));

    }

    @Test
    public void shouldNotMigrateFromHostedEngineHost() {
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2, vm3, vm5, vm6));

        vm6.setOrigin(OriginType.HOSTED_ENGINE);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm1, vm2, vm3)
        ));

        vm6.setOrigin(OriginType.RHEV);
        vm3.setOrigin(OriginType.HOSTED_ENGINE);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm5, vm6)
        ));
    }

    @Test
    public void shouldNotMigrateFromHostWithPinnedVM() {
        affinityGroups.add(createAffinityGroup(cluster, EntityAffinityRule.POSITIVE, vm1, vm2, vm3, vm5, vm6));

        vm6.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm1, vm2, vm3)
        ));

        vm6.setMigrationSupport(MigrationSupport.MIGRATABLE);
        vm3.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm5, vm6)
        ));
    }

    @Test
    public void shouldMigrateBasedOnLabel() {
        cluster.setCompatibilityVersion(Version.v4_3);

        labels.add(new LabelBuilder()
                .id(Guid.newGuid())
                .name("Test Lable")
                .entities(vm1, vm4, vm5, host1)
                .implicitAffinityGroup(true)
                .build()
        );

        assertVmsToMigrateGroups(Arrays.asList(
                Arrays.asList(vm4, vm5)
        ));
    }

    private Cluster createCluster() {
        Guid id = Guid.newGuid();
        Cluster cluster = new Cluster();
        cluster.setClusterId(id);
        cluster.setId(id);
        cluster.setName("Default cluster");
        cluster.setCompatibilityVersion(Version.getLast());
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

    private AffinityGroup createAffinityGroup(Cluster cluster, EntityAffinityRule vmAffinityRule, final
    VM... vmList) {
        AffinityGroup ag =
                new AffinityGroup();
        ag.setId(Guid.newGuid());
        ag.setVmAffinityRule(vmAffinityRule);
        ag.setClusterId(cluster.getId());
        ag.setVmEnforcing(true);
        ag.setVmIds(Arrays.stream(vmList).map(VM::getId).collect(Collectors.toList()));
        return ag;
    }

    private AffinityGroup createAffinityGroup(Cluster cluster,
            EntityAffinityRule vmAffinityRule,
            EntityAffinityRule vdsRule,
            boolean isVdsEnforcing,
            List<VDS> vdsList,
            VM... vmList) {
        AffinityGroup ag = createAffinityGroup(cluster, vmAffinityRule, vmList);
        ag.setVdsIds(vdsList.stream().map(VDS::getId).collect(Collectors.toList()));
        ag.setVdsAffinityRule(vdsRule);
        ag.setVdsEnforcing(isVdsEnforcing);
        return ag;
    }

    private void prepareVmDao(VM... vmList) {
        doAnswer(invocation -> {
            final List<VM> selectedVms = new ArrayList<>();
            final Set<Guid> vmIds = new HashSet<>(invocation.getArgument(0));
            for (VM vm : vmList) {
                if (vmIds.contains(vm.getId())) {
                    selectedVms.add(vm);
                }
            }
            return selectedVms;
        }).when(vmDao).getVmsByIds(any());
    }

    private List<AffinityGroup> copyGroups() {
        return affinityGroups.stream()
                .map(AffinityGroup::new)
                .collect(Collectors.toList());
    }

    private List<VM> getVmsToMigrate() {
        List<VM> res = new ArrayList<>();
        enforcer.chooseVmsToMigrate(cluster).forEachRemaining(res::add);
        return res;
    }

    private void assertVmsToMigrateGroups(List<List<VM>> groups) {
        int size = groups.stream()
                .mapToInt(Collection::size)
                .sum();

        List<VM> vmsToMigrate = getVmsToMigrate();
        assertThat(vmsToMigrate).hasSize(size);

        int vmIndex = 0;
        for (List<VM> group : groups) {
            int groupSize = group.size();
            assertThat(vmsToMigrate.subList(vmIndex, vmIndex + groupSize)).containsOnlyElementsOf(group);
            vmIndex += groupSize;
        }
    }
}
