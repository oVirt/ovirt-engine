package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({ MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class HighPerformanceCpuPolicyUnitTest {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxSchedulerWeight, 1000)
        );
    }

    private VDS host1;
    private VDS host2;
    private VDS host3;
    private VDS host4;
    private VDS host5;

    private VM vm;

    @InjectMocks
    private HighPerformanceCpuPolicyUnit unit = new HighPerformanceCpuPolicyUnit(null, null);

    @BeforeEach
    public void setUp() {
        host1 = createHost(8, 1, 1);
        host2 = createHost(4, 2, 1);
        host3 = createHost(2, 2, 2);
        host4 = createHost(1, 4, 2);
        host5 = createHost(4, 4, 4);

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(2);
        vm.setThreadsPerCpu(2);
        vm.setvNumaNodeList(Collections.emptyList());
    }

    @Test
    public void testScoreRegularVm() {
        vm.setVmType(VmType.Server);

        assertThat(score()).extracting("first", "second").contains(
                tuple(host1.getId(), 1),
                tuple(host2.getId(), 1),
                tuple(host3.getId(), 1),
                tuple(host4.getId(), 1),
                tuple(host5.getId(), 1)
        );
    }

    @Test
    public void testScoreHighPerformanceVm() {
        vm.setVmType(VmType.HighPerformance);

        assertThat(score()).extracting("first", "second").contains(
                tuple(host1.getId(), 1000),
                tuple(host2.getId(), 1000),
                tuple(host3.getId(), 1),
                tuple(host4.getId(), 1000),
                tuple(host5.getId(), 1)
        );
    }

    private List<Pair<Guid, Integer>> score() {
        return unit.score(new SchedulingContext(new Cluster(), Collections.emptyMap()), Arrays.asList(host1, host2, host3, host4, host5), vm);
    }

    private VDS createHost(int numOfSockets, int coresPerSocket, int threadsPerCore) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setCpuSockets(numOfSockets);
        host.setCpuCores(numOfSockets * coresPerSocket);
        host.setCpuThreads(numOfSockets * coresPerSocket * threadsPerCore);
        return host;
    }
}
