package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingParameters;
import org.ovirt.engine.core.bll.utils.NumaUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class NumaWeightPolicyUnitTest {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxSchedulerWeight, 1000)
        );
    }

    @Mock
    private VmNumaNodeDao vmNumaNodeDao;

    @Mock
    private NumaUtils numaUtils;

    @InjectMocks
    private NumaWeightPolicyUnit unit = new NumaWeightPolicyUnit(null,  null);

    private VM vm;
    private VDS host1;
    private VDS host2;
    private VDS host3;

    @BeforeEach
    public void setUp() {
        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setVmMemSizeMb(1024);
        vm.setCpuPerSocket(2);

        host1 = createHost(true, 2L);
        host2 = createHost(true, 0L);
        host3 = createHost(false, 0L);
    }

    @Test
    public void testVmWithoutNumaNodes() {
        when(vmNumaNodeDao.getAllVmNumaNodeByVmId(any(Guid.class))).thenReturn(Collections.emptyList());

        assertThat(score()).extracting("first", "second").contains(
                tuple(host1.getId(), 1),
                tuple(host2.getId(), 1000),
                tuple(host3.getId(), 1)
        );
    }

    @Test
    public void testVmWithNumaNodes() {
        when(vmNumaNodeDao.getAllVmNumaNodeByVmId(any(Guid.class))).thenReturn(
                Arrays.asList(new VmNumaNode(), new VmNumaNode())
        );

        assertThat(score()).extracting("first", "second").contains(
                tuple(host1.getId(), 1),
                tuple(host2.getId(), 1),
                tuple(host3.getId(), 1)
        );
    }

    private List<Pair<Guid, Integer>> score() {
        return unit.score(new SchedulingContext(new Cluster(), Collections.emptyMap(), new SchedulingParameters()), Arrays.asList(host1, host2, host3), vm);
    }

    private VDS createHost(boolean numaSupport, long nodeCountWhereVmFits) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setNumaSupport(numaSupport);
        doReturn(nodeCountWhereVmFits).when(numaUtils).countNumaNodesWhereVmFits(any(VmStatic.class), eq(host.getId()));
        return host;
    }
}
