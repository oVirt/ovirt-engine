package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CPUPolicyUnitTest {
    @Mock
    private VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;

    @Mock
    private PendingResourceManager pendingHostResources;

    @Mock
    private VdsManager vdsManager;

    @Mock
    private ResourceManager resourceManager;

    @InjectMocks
    private final CPUPolicyUnit cpuPolicyUnit = new CPUPolicyUnit(null, pendingHostResources);

    private VDS vdsWithInvalidCpuInfo;

    private VDS vdsWithCores;

    private VM vm;

    private Cluster cluster;

    @BeforeEach
    public void setUp() {
        vm = new VM();
        vm.setCpuPerSocket(2);
        vm.setNumOfSockets(2);

        vdsWithInvalidCpuInfo = new VDS();
        vdsWithInvalidCpuInfo.setId(Guid.newGuid());
        vdsWithInvalidCpuInfo.setCpuCores(0);
        vdsWithInvalidCpuInfo.setCpuThreads(0);

        vdsWithCores = new VDS();
        vdsWithCores.setId(Guid.newGuid());
        vdsWithCores.setCpuSockets(1);
        vdsWithCores.setCpuCores(2);
        vdsWithCores.setCpuThreads(4);

        cluster = new Cluster();
        cluster.setId(Guid.newGuid());

        when(pendingHostResources.pendingHostResources(any(), any())).thenReturn(Collections.emptyList());
        when(resourceManager.getVdsManager(any())).thenReturn(vdsManager);
    }

    /**
     * When obviously wrong information is reported, skip the host
     */
    @Test
    public void shouldFilterInvalidHosts() {
        cluster.setCountThreadsAsCores(true);
        final List<VDS> candidates = filter();
        assertThat(candidates).containsOnly(vdsWithCores);
    }

    @Test
    public void shouldFilterHostWithNotEnoughThreads() {
        cluster.setCountThreadsAsCores(true);
        vdsWithCores.setCpuThreads(3);
        final List<VDS> candidates = filter();
        assertThat(candidates).doesNotContain(vdsWithCores);
    }

    @Test
    public void shouldFilterHostsWithNotEnoughCpuCores() {
        cluster.setCountThreadsAsCores(false);
        final List<VDS> candidates = filter();
        assertThat(candidates).doesNotContain(vdsWithCores);
    }

    @Test
    public void shouldKeepHostsWithEnoughCpuCores() {
        cluster.setCountThreadsAsCores(false);
        vdsWithCores.setCpuCores(4);
        final List<VDS> candidates = filter();
        assertThat(candidates).contains(vdsWithCores);
    }

    @Test
    public void shouldKeepHostsWithEnoughThreads() {
        cluster.setCountThreadsAsCores(true);
        final List<VDS> candidates = filter();
        assertThat(candidates).contains(vdsWithCores);
    }

    private List<VDS> filter() {
        return cpuPolicyUnit.filter(new SchedulingContext(cluster, Collections.emptyMap()),
                Arrays.asList(vdsWithInvalidCpuInfo, vdsWithCores),
                Collections.singletonList(vm),
                mock(PerHostMessages.class));
    }

}
