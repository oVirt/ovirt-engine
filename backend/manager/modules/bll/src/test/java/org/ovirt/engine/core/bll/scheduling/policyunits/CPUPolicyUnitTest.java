package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

@RunWith(MockitoJUnitRunner.class)
public class CPUPolicyUnitTest {

    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, Integer.MAX_VALUE)
    );

    @Mock
    private ClusterDao clusterDao;

    @InjectMocks
    private final CPUPolicyUnit cpuPolicyUnit = new CPUPolicyUnit(null, null);

    private VDS vdsWithInvalidCpuInfo;

    private VDS vdsWithCores;

    private VM vm;

    private Cluster cluster;

    @Before
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
        when(clusterDao.get(any(Guid.class))).thenReturn(cluster);
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
        return cpuPolicyUnit.filter(cluster,
                Arrays.asList(vdsWithInvalidCpuInfo, vdsWithCores),
                vm,
                null, mock(PerHostMessages.class));
    }

}
