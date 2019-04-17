package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;

public class CPUPolicyUnitTest {
    private final CPUPolicyUnit cpuPolicyUnit = new CPUPolicyUnit(null, null);

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
        vdsWithCores.setCpuCores(2);
        vdsWithCores.setCpuThreads(4);

        cluster = new Cluster();
        cluster.setId(Guid.newGuid());
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

    /**
     * When no cpu information at all is available, consider the host
     */
    @Test
    public void shouldKeepHostsWithNoCpuInformation() {
        vdsWithInvalidCpuInfo.setCpuCores(null);
        final List<VDS> candidates = filter();
        assertThat(candidates).contains(vdsWithInvalidCpuInfo);
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
                vm, mock(PerHostMessages.class));
    }

}
