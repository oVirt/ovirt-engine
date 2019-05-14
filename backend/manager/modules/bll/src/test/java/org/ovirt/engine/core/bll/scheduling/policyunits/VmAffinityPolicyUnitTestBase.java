package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class VmAffinityPolicyUnitTestBase {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.MaxSchedulerWeight, 1000));
    }

    @Mock
    protected PendingResourceManager pendingResourceManager;
    @Mock
    protected AffinityGroupDao affinityGroupDao;
    @Mock
    protected VmDao vmDao;

    protected Cluster cluster;
    protected SchedulingContext context;
    protected VDS host1;
    protected VDS host2;
    protected VDS host3;

    protected VM newVm;

    protected List<AffinityGroup> affinityGroups = new ArrayList<>();
    protected List<VM> runningVMs = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        cluster = new Cluster();
        cluster.setId(Guid.newGuid());

        context = new SchedulingContext(cluster, Collections.emptyMap());

        host1 = createHost(cluster);
        host2 = createHost(cluster);
        host3 = createHost(cluster);

        newVm = createVMDown(cluster);

        when(pendingResourceManager.pendingResources(any())).thenReturn(Collections.emptyList());
        when(affinityGroupDao.getAllAffinityGroupsWithFlatLabelsByVmId(any())).thenReturn(affinityGroups);
        when(vmDao.getAllRunningByCluster(any())).thenReturn(runningVMs);
    }

    protected VDS createHost(Cluster cluster) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setClusterId(cluster.getId());
        return vds;
    }

    protected VM createVmRunning(VDS host) {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterId(host.getClusterId());
        vm.setRunOnVds(host.getId());
        vm.setStatus(VMStatus.Up);

        runningVMs.add(vm);

        return vm;
    }

    protected VM createVMDown(Cluster cluster) {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterId(cluster.getId());
        vm.setStatus(VMStatus.Down);
        return vm;
    }

    protected AffinityGroup createAffinityGroup(Cluster cluster,
            EntityAffinityRule vmAffinityRule,
            boolean enforcing,
            final VM... vmList) {
        return createAffinityGroup(cluster, vmAffinityRule, enforcing, 1.0, vmList);
    }

    protected AffinityGroup createAffinityGroup(Cluster cluster,
            EntityAffinityRule vmAffinityRule,
            boolean enforcing,
            double priority,
            final VM... vmList) {
        AffinityGroup ag = new AffinityGroup();
        ag.setId(Guid.newGuid());
        ag.setVmAffinityRule(vmAffinityRule);
        ag.setClusterId(cluster.getId());
        ag.setVmEnforcing(enforcing);
        ag.setPriorityFromDouble(priority);
        ag.setVmIds(Arrays.stream(vmList).map(VM::getId).collect(Collectors.toList()));
        return ag;
    }
}
