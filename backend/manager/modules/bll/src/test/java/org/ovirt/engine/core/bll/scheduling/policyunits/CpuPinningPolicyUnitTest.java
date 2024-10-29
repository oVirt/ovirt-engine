package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.VdsCpuUnitPinningHelper;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CpuPinningPolicyUnitTest {
    private VDS hostWithCpus;

    private VDS hostWithoutCpus;

    private VM vm;

    @Mock
    private VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;

    @Mock
    private PendingResourceManager pendingResourceManager;

    @InjectMocks
    private final CpuPinningPolicyUnit policyUnit = new CpuPinningPolicyUnit(null, pendingResourceManager);

    private PerHostMessages perHostMessages;

    private Cluster cluster;

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private VdsManager vdsManager;

    @BeforeEach
    public void setUp() {
        hostWithCpus = new VDS();
        hostWithCpus.setId(Guid.newGuid());
        hostWithCpus.setOnlineCpus("0, 1, 2,3, 5");

        hostWithoutCpus = new VDS();
        hostWithoutCpus.setId(Guid.newGuid());
        hostWithoutCpus.setOnlineCpus(null);

        perHostMessages = new PerHostMessages();

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        cluster = new Cluster();
        doReturn(vdsManager).when(resourceManager).getVdsManager(any());
        doReturn(new ArrayList<>()).when(vdsManager).getCpuTopology();
        when(pendingResourceManager.pendingHostResources(any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    public void shouldHandleEmptyHostList() {
        vm.setCpuPinning("1#0_2#3");
        final List<VDS> filteredHost = policyUnit.filter(new SchedulingContext(cluster, Collections.emptyMap()), new ArrayList<>(), vm, mock(PerHostMessages.class));
        assertThat(filteredHost, is(empty()));
        assertThat(messages(), is(empty()));
    }

    /**
     * No online CPU is equivalent to no information available
     */
    @Test
    public void shouldConsiderHostsWithNoOnlineCpus() {
        vm.setCpuPinningPolicy(CpuPinningPolicy.NONE);
        hostWithoutCpus.setOnlineCpus("");
        assertThat(filter(), hasItem(hostWithoutCpus));
        assertThat(messages(), is(empty()));
    }

    /**
     * No information about online cpus is available
     */
    @Test
    public void shouldConsiderHostWithNoCpuData() {
        vm.setCpuPinningPolicy(CpuPinningPolicy.NONE);
        hostWithoutCpus.setOnlineCpus(null);
        assertThat(filter(), hasItem(hostWithoutCpus));
        assertThat(messages(), is(empty()));
    }

    @Test
    public void shouldReturnAllHostWithoutPinningRequirements() {
        vm.setCpuPinningPolicy(CpuPinningPolicy.NONE);
        assertThat(filter(), containsInAnyOrder(hostWithCpus, hostWithoutCpus));
        assertThat(messages(), is(empty()));
    }

    @Test
    public void shouldDetectOfflineCpu() {
        vm.setCpuPinning("1#0_2#4");
        assertThat(filter(), not(hasItem(hostWithCpus)));
        assertThat(messages(), hasSize(2));
    }

    @Test
    public void shouldExcludeHostWithoutCpuInformation() {
        vm.setCpuPinning("1#0_2#4");
        assertThat(filter(), not(hasItem(hostWithoutCpus)));
    }

    @Test
    public void shouldHandleExcludedCpu() {
        vm.setCpuPinning("1#3-5,^4");
        assertThat(filter(), hasItem(hostWithCpus));
        assertThat(messages(), hasSize(1));
    }

    @Test
    public void shouldDetectNonExistentCpu() {
        vm.setCpuPinning("1#6");
        assertThat(filter(), not(hasItem(hostWithCpus)));
        assertThat(messages(), hasSize(2));
    }

    @Test
    public void shouldFindSuitableHost() {
        vm.setCpuPinning("1#0_2#3");
        assertThat(filter(), hasItem(hostWithCpus));
        assertThat(messages(), hasSize(1));
    }

    private List<VDS> filter() {
        return policyUnit.filter(new SchedulingContext(cluster, Collections.emptyMap()),
                Arrays.asList(hostWithCpus, hostWithoutCpus), Collections.singletonList(vm),
                perHostMessages);
    }

    private Collection<List<String>> messages() {
        return perHostMessages.getMessages().values();
    }

}
