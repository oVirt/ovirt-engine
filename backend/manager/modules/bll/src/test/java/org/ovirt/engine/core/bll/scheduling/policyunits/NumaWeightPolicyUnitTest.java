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
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class NumaWeightPolicyUnitTest extends NumaPolicyTestBase{

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxSchedulerWeight, 1000)
        );
    }

    @InjectMocks
    private NumaWeightPolicyUnit unit = new NumaWeightPolicyUnit(null,  null);

    private VM vm;
    private VDS host1;
    private VDS host2;
    private VDS host3;
    private VDS host4;

    @BeforeEach
    public void setUp() {
        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setVmMemSizeMb(1024);
        vm.setCpuPerSocket(2);
        vm.setvNumaNodeList(Collections.emptyList());

        host1 = createHost(4, 2048, 2);
        host2 = createHost(4, 512, 2);
        host3 = createHost(4, 2048, 1);
        host4 = createHost(0, 0);
    }

    @Test
    public void testVmWithoutNumaNodes() {
        vm.setvNumaNodeList(Collections.emptyList());

        assertThat(score()).extracting("first", "second").contains(
                tuple(host1.getId(), 1),
                tuple(host2.getId(), 1000),
                tuple(host3.getId(), 1000),
                tuple(host4.getId(), 1)
        );
    }

    @Test
    public void testVmWithNumaNodes() {
        vm.setvNumaNodeList(Arrays.asList(new VmNumaNode(), new VmNumaNode()));

        assertThat(score()).extracting("first", "second").contains(
                tuple(host1.getId(), 1),
                tuple(host2.getId(), 1),
                tuple(host3.getId(), 1),
                tuple(host4.getId(), 1)
        );
    }

    private List<Pair<Guid, Integer>> score() {
        return unit.score(new SchedulingContext(new Cluster(), Collections.emptyMap()), Arrays.asList(host1, host2, host3, host4), vm);
    }
}
