package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.LabelDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmToHostAffinityFilterPolicyUnitTest extends VmToHostAffinityPolicyUnitBaseTest {

    @Mock
    private LabelDao labelDao;

    private List<Label> labels;

    @InjectMocks
    VmToHostAffinityFilterPolicyUnit unit =
            new VmToHostAffinityFilterPolicyUnit(null, null);

    @BeforeEach
    public void setUpLabelDao() {
        labels = new ArrayList<>();
        when(labelDao.getAllByEntityIds(any())).thenReturn(labels);
    }

    @Test
    public void testNoAffinityGroups() {
        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = new ArrayList<>();
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).contains(
                host_positive_enforcing,
                host_negative_enforcing,
                host_not_in_affinity_group);
    }

    @Test
    public void testPositiveAffinity() {
        hosts = Arrays.asList(host_positive_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = Arrays.asList(positive_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).contains
                (host_positive_enforcing).doesNotContain(host_not_in_affinity_group);
    }

    @Test
    public void testNegativeAffinity() {
        hosts = Arrays.asList(host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = Arrays.asList(negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).contains
                (host_not_in_affinity_group);
    }

    @Test
    public void testNegativeAndPositiveAffinity() {
        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = Arrays.asList(positive_enforcing_group, negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .contains(host_positive_enforcing)
                .doesNotContain(host_negative_enforcing);
    }

    @Test
    public void testWithAffinityIntersection() {

        AffinityGroup positiveCollisionGroup = new AffinityGroup();
        positiveCollisionGroup.setVdsIds(Arrays.asList(host_negative_enforcing.getId()));
        positiveCollisionGroup.setVdsAffinityRule(EntityAffinityRule.POSITIVE);
        positiveCollisionGroup.setVdsEnforcing(true);

        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = Arrays.asList(positiveCollisionGroup, negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsWithFlatLabelsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).isEmpty();
    }

    @Test
    public void testLabelSimple() {
        cluster.setCompatibilityVersion(Version.v4_3);
        hosts = Arrays.asList(host_positive_enforcing, host_not_in_affinity_group);

        labels.add(new LabelBuilder()
                .entities(vm, host_positive_enforcing)
                .implicitAffinityGroup(true)
                .build());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .contains(host_positive_enforcing)
                .doesNotContain(host_not_in_affinity_group);
    }

    @Test
    public void testLabelIgnoredForNewVersion() {
        cluster.setCompatibilityVersion(Version.getLast());
        hosts = Arrays.asList(host_positive_enforcing, host_not_in_affinity_group);

        labels.add(new LabelBuilder()
                .entities(vm, host_positive_enforcing)
                .implicitAffinityGroup(true)
                .build());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .contains(host_positive_enforcing)
                .contains(host_not_in_affinity_group);
    }

    @Test
    public void testLabelNoValidHost() {
        cluster.setCompatibilityVersion(Version.v4_3);
        hosts = Arrays.asList(host_positive_enforcing, host_not_in_affinity_group);

        labels.add(new LabelBuilder()
                .entities(vm, host_positive_enforcing)
                .implicitAffinityGroup(true)
                .build());

        labels.add(new LabelBuilder()
                .entities(vm, host_not_in_affinity_group)
                .implicitAffinityGroup(true)
                .build());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .isEmpty();
    }

    @Test
    public void testLabelWithHostsOnly() {
        cluster.setCompatibilityVersion(Version.v4_3);
        hosts = Arrays.asList(host_positive_enforcing, host_not_in_affinity_group);

        labels.add(new LabelBuilder()
                .entities(vm, host_positive_enforcing)
                .implicitAffinityGroup(true)
                .build());

        labels.add(new LabelBuilder()
                .entities(host_positive_enforcing, host_not_in_affinity_group)
                .implicitAffinityGroup(true)
                .build());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .contains(host_positive_enforcing)
                .doesNotContain(host_not_in_affinity_group);
    }
}
