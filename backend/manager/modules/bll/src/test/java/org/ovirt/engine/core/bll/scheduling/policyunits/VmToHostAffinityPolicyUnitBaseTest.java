package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public abstract class VmToHostAffinityPolicyUnitBaseTest {
    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule();

    @Mock
    AffinityGroupDao affinityGroupDao;

    protected Cluster cluster;
    protected VM vm;
    protected VDS host_positive_enforcing;
    protected VDS host_negative_enforcing;
    protected VDS host_not_in_affinity_group;
    protected List<VDS> hosts;
    protected AffinityGroup positive_enforcing_group;
    protected AffinityGroup negative_enforcing_group;

    @Before
    public void setUp() throws Exception {
        cluster = new Cluster();
        cluster.setId(Guid.newGuid());

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterId(cluster.getId());

        host_positive_enforcing = new VDS();
        host_positive_enforcing.setId(Guid.newGuid());
        host_positive_enforcing.setClusterId(cluster.getId());

        host_negative_enforcing = new VDS();
        host_negative_enforcing.setId(Guid.newGuid());
        host_negative_enforcing.setClusterId(cluster.getId());

        host_not_in_affinity_group = new VDS();
        host_not_in_affinity_group.setId(Guid.newGuid());
        host_not_in_affinity_group.setClusterId(cluster.getId());

        positive_enforcing_group = new AffinityGroup();
        negative_enforcing_group = new AffinityGroup();

        positive_enforcing_group.setVdsIds(Arrays.asList(host_positive_enforcing.getId()));
        positive_enforcing_group.setVdsAffinityRule(EntityAffinityRule.POSITIVE);
        positive_enforcing_group.setVdsEnforcing(true);

        negative_enforcing_group.setVdsIds(Arrays.asList(host_negative_enforcing.getId()));
        negative_enforcing_group.setVdsAffinityRule(EntityAffinityRule.NEGATIVE);
        negative_enforcing_group.setVdsEnforcing(true);

    }

}

