package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NumaPolicyUnitTest extends NumaPolicyTestBase{

    private static final long NODE_SIZE = 1024;

    public VM vm;

    public VDS hostWithoutNuma;
    public VDS hostTwoNodes;
    public VDS hostFourNodes;

    public List<VDS> hosts;

    public PendingResourceManager pendingResourceManager = new PendingResourceManager();

    @InjectMocks
    public NumaPolicyUnit unit = new NumaPolicyUnit(null, pendingResourceManager);

    @BeforeEach
    public void setUp() {
        vm = new VM();
        vm.setId(Guid.newGuid());

        hostWithoutNuma = createHost(0, NODE_SIZE);
        hostTwoNodes = createHost(2, NODE_SIZE);
        hostFourNodes = createHost(4, NODE_SIZE);

        hosts = Arrays.asList(hostWithoutNuma, hostTwoNodes, hostFourNodes);
    }

    @Test
    public void testNoNumaNodes() {
        vm.setvNumaNodeList(Collections.emptyList());

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostWithoutNuma, hostTwoNodes, hostFourNodes);
    }

    @Test
    public void testInterleaveMode() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE, 0, Arrays.asList(0))
        ));
        vm.getvNumaNodeList().forEach(node -> node.setNumaTuneMode(NumaTuneMode.INTERLEAVE));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostTwoNodes, hostFourNodes);
    }

    @Test
    public void testNumaNodesWithoutPinning() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE, 0, Collections.emptyList()),
                createVmNode(NODE_SIZE, 1, Collections.emptyList())
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostWithoutNuma, hostTwoNodes, hostFourNodes);
    }

    @Test
    public void testNodePinnedToFirst() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE, 0, Arrays.asList(0))
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostTwoNodes, hostFourNodes);
    }

    @Test
    public void testNodePinnedToFourth() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE, 0, Arrays.asList(3))
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostFourNodes);
    }

    @Test
    public void testThreeNodes() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE, 0, Arrays.asList(0)),
                createVmNode(NODE_SIZE, 1, Arrays.asList(1)),
                createVmNode(NODE_SIZE, 2, Arrays.asList(2))
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostFourNodes);
    }

    @Test
    public void testThreeNodesWithOneUnpinned() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE, 0, Arrays.asList(0)),
                createVmNode(NODE_SIZE, 1, Arrays.asList(1)),
                createVmNode(NODE_SIZE, 2, Collections.emptyList())
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostTwoNodes, hostFourNodes);
    }

    @Test
    public void testSmallNodesPinnedToOne() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE / 4, 0, Arrays.asList(0)),
                createVmNode(NODE_SIZE / 4, 1, Arrays.asList(0)),
                createVmNode(NODE_SIZE / 4, 2, Arrays.asList(0))
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostTwoNodes, hostFourNodes);
    }

    @Test
    public void testSmallNodesPinnedToOneFail() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE / 4, 0, Arrays.asList(0)),
                createVmNode(NODE_SIZE / 4, 1, Arrays.asList(0)),
                createVmNode(NODE_SIZE / 4, 2, Arrays.asList(0)),

                createVmNode(NODE_SIZE / 4, 3, Arrays.asList(0)),
                createVmNode(NODE_SIZE / 4, 3, Arrays.asList(0)),
                createVmNode(NODE_SIZE / 4, 4, Arrays.asList(0))
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).isEmpty();
    }

    @Test
    public void testSmallNodesPinnedToTwo() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE / 4, 0, Arrays.asList(0, 1)),
                createVmNode(NODE_SIZE / 4, 1, Arrays.asList(0, 1)),
                createVmNode(NODE_SIZE / 4, 2, Arrays.asList(0, 1)),

                createVmNode(NODE_SIZE / 4, 3, Arrays.asList(0, 1)),
                createVmNode(NODE_SIZE / 4, 4, Arrays.asList(0, 1)),
                createVmNode(NODE_SIZE / 4, 5, Arrays.asList(0, 1))
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostTwoNodes, hostFourNodes);
    }

    @Test
    public void testSmallNodesPinnedToFour() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE / 4, 0, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 1, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 2, Arrays.asList(0, 1, 2, 3)),

                createVmNode(NODE_SIZE / 4, 3, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 4, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 5, Arrays.asList(0, 1, 2, 3))
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostFourNodes);
    }

    @Test
    public void testManySmallNodesPinnedToFour() {
        vm.setvNumaNodeList(Arrays.asList(
                createVmNode(NODE_SIZE / 4, 0, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 1, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 2, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 3, Arrays.asList(0, 1, 2, 3)),

                createVmNode(NODE_SIZE / 4, 4, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 5, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 6, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 7, Arrays.asList(0, 1, 2, 3)),

                createVmNode(NODE_SIZE / 4, 8, Arrays.asList(0, 1, 2, 3)),
                createVmNode(NODE_SIZE / 4, 9, Arrays.asList(0, 1, 2, 3))
        ));

        List<VDS> passedHosts = filter();
        assertThat(passedHosts).containsOnly(hostFourNodes);
    }

    private List<VDS> filter() {
        return unit.filter(new SchedulingContext(new Cluster(), Collections.emptyMap()),
                hosts,
                Collections.singletonList(vm),
                new PerHostMessages());
    }
}
