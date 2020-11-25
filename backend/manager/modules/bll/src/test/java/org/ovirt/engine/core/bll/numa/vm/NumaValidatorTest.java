package org.ovirt.engine.core.bll.numa.vm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVdsDao;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVdsNumaNodeDao;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class NumaValidatorTest {

    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;

    @Mock
    private VdsDao vdsDao;

    @InjectMocks
    private NumaValidator underTest;

    private ArrayList<VdsNumaNode> vdsNumaNodes_host1;
    private ArrayList<VdsNumaNode> vdsNumaNodes_host2;
    private VM vm;
    private ArrayList<VmNumaNode> vmNumaNodes;
    private Map<Guid, List<VdsNumaNode>> hostNumaNodesMap;

    @BeforeEach
    public void setUp() {

        vdsNumaNodes_host1 = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2), createVdsNumaNode(3)));
        vmNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(0, vdsNumaNodes_host1), createVmNumaNode(1)));
        mockVdsNumaNodeDao(vdsNumaNodeDao, vdsNumaNodes_host1);
        mockVdsDao(vdsDao);

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setDedicatedVmForVdsList(Collections.singletonList(Guid.newGuid()));
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(2);
        vm.setVmMemSizeMb(4000);
        vm.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vm.getvNumaNodeList().forEach(node -> node.setNumaTuneMode(NumaTuneMode.INTERLEAVE));
        vm.setvNumaNodeList(vmNumaNodes);
    }

    @Test
    public void shouldSetNumaPinning() {
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldHandleNoNumaNodes() {
        vm.setvNumaNodeList(null);
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldDetectHostWihtoutNumaSupport() {
        vdsNumaNodes_host1 = new ArrayList(Collections.singletonList(createVdsNumaNode(1)));
        hostNumaNodesMap = new HashMap<>();
        hostNumaNodesMap.put(Guid.newGuid(), vdsNumaNodes_host1);

        assertValidationFailure(underTest.validateNumaCompatibility(vm, vm.getvNumaNodeList(), hostNumaNodesMap),
                EngineMessage.HOST_NUMA_NOT_SUPPORTED);
    }

    @Test
    public void shouldDetectHostWithoutNumaSupportWithTwoPinnedHosts() {
        vdsNumaNodes_host1 = new ArrayList<>(Arrays.asList(createVdsNumaNode(1)));
        vdsNumaNodes_host2 = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2), createVdsNumaNode(3)));
        hostNumaNodesMap = new HashMap<>();
        hostNumaNodesMap.put(Guid.newGuid(), vdsNumaNodes_host1);
        hostNumaNodesMap.put(Guid.newGuid(), vdsNumaNodes_host1);

        assertValidationFailure(underTest.validateNumaCompatibility(vm, vm.getvNumaNodeList(), hostNumaNodesMap),
                EngineMessage.HOST_NUMA_NOT_SUPPORTED);
    }

    @Test
    public void shouldDetectZeroHostNodes() {
        vdsNumaNodes_host1.clear();
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }

    @Test
    public void shouldDetectInsufficientMemory() {
        vmNumaNodes.get(0).setNumaTuneMode(NumaTuneMode.STRICT);
        vmNumaNodes.get(0).setMemTotal(1000);
        vdsNumaNodes_host1.get(0).setMemTotal(500);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_MEMORY_ERROR);
    }

    @Test
    public void shouldDetectTooMuchVmNumaNodes() {
        vm.getvNumaNodeList().forEach(node -> node.setNumaTuneMode(NumaTuneMode.PREFERRED));
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE);
    }

    @Test
    public void shouldDetectTooMuchHostNodes() {
        VmNumaNode vmNumaNode = createVmNumaNode(1, vdsNumaNodes_host1);
        vmNumaNode.setNumaTuneMode(NumaTuneMode.PREFERRED);
        vm.setvNumaNodeList(Collections.singletonList(vmNumaNode));
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE);
    }

    @Test
    public void shouldDetectDuplicateNodeIndex() {
        vmNumaNodes.get(0).setIndex(1);
        vmNumaNodes.get(1).setIndex(1);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_INDEX_DUPLICATE);
    }

    @Test
    public void shouldDetectNonContinuousNodeIndices() {
        vmNumaNodes.get(0).setIndex(0);
        vmNumaNodes.get(1).setIndex(2);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_NON_CONTINUOUS_INDEX);
    }

    @Test
    public void shouldDetectIndicesNotStartingWithZero() {
        vmNumaNodes.get(0).setIndex(2);
        vmNumaNodes.get(1).setIndex(3);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_NON_CONTINUOUS_INDEX);
    }

    @Test
    public void shouldValidateSingleNodePinning() {
        vm.setvNumaNodeList(Collections.singletonList(createVmNumaNode(0, Collections.singletonList(createVdsNumaNode(1)))));
        vm.getvNumaNodeList().forEach(node -> node.setNumaTuneMode(NumaTuneMode.PREFERRED));
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldDetectMissingPinningEntry() {
        vm.getvNumaNodeList().get(0).getVdsNumaNodeList().set(0, null);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_PINNED_INDEX_ERROR);
    }

    @Test
    public void shouldDetectSufficientMemory() {
        vm.getvNumaNodeList().forEach(node -> node.setNumaTuneMode(NumaTuneMode.STRICT));
        vmNumaNodes.get(0).setMemTotal(1000);
        vdsNumaNodes_host1.get(0).setMemTotal(2000);
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldDetectMissingRequiredHostNumaNodes() {
        vdsNumaNodes_host1.remove(0);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_HOST_NODE_INVALID_INDEX);
    }

    @Test
    public void shouldNotDoWithoutPinnedHost() {
        vm.setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);

        vm.setDedicatedVmForVdsList(new ArrayList<>());
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()), EngineMessage
                .ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_AT_LEAST_ONE_HOST);
    }

    @Test
    public void shouldDoWithTwoPinnedHost() {
        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldCreateAsMuchNumaNodesAsVirtualCores() {
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(2);
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldCreateLessNumaNodesAsVirtualCores() {
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(3);
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void failCreateMoreNumaNodesThanVirtualCores() {
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(1);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_MORE_NODES_THAN_CPUS);
    }

    @Test
    public void shouldDetectDuplicateCpuAssignment() {
        vmNumaNodes.get(0).setCpuIds(Collections.singletonList(0));
        vmNumaNodes.get(1).setCpuIds(Collections.singletonList(0));
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_DUPLICATE_CPU_IDS);
    }

    @Test
    public void shouldDetectInvalidCpuIndex() {
        vmNumaNodes.get(0).setCpuIds(Collections.singletonList(vm.getNumOfCpus() + 1));
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_INVALID_CPU_ID);
    }

    @Test
    public void shouldDetectNegativeCpuIndex() {
        vmNumaNodes.get(0).setCpuIds(Collections.singletonList(-1));
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_INVALID_CPU_ID);
    }

    @Test
    public void shouldDetectMoreNumaMemoryThanVmMemory() {
        vm.setVmMemSizeMb(1000);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_TOTAL_MEMORY_ERROR);
    }

    @Test
    public void shouldDetectHugepagesNotFitNodeMemory() {
        vm.setCustomProperties("hugepages=1048576");
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_NOT_MULTIPLE_OF_HUGEPAGE);
    }

    @Test
    public void shouldSucceedIfHugepagesFit() {
        vm.setCustomProperties("hugepages=512000");
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    private void assertValidationFailure(ValidationResult validationResult, EngineMessage engineMessage) {
        assertThat(validationResult, failsWith(engineMessage));
    }
}
