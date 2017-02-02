package org.ovirt.engine.core.bll.numa.vm;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVdsNumaNodeDao;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class NumaValidatorTest {

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;

    @InjectMocks
    private NumaValidator underTest;

    private ArrayList<VdsNumaNode> vdsNumaNodes;
    private VM vm;
    private ArrayList<VmNumaNode> vmNumaNodes;

    @Before
    public void setUp() throws Exception {

        vdsNumaNodes = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2), createVdsNumaNode(3)));
        vmNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(0, vdsNumaNodes), createVmNumaNode(1)));
        mockVdsNumaNodeDao(vdsNumaNodeDao, vdsNumaNodes);

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setDedicatedVmForVdsList(Collections.singletonList(Guid.newGuid()));
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(2);
        vm.setVmMemSizeMb(4000);
        vm.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vm.setNumaTuneMode(NumaTuneMode.INTERLEAVE);
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
        vdsNumaNodes = new ArrayList(Collections.singletonList(createVdsNumaNode(1)));
        assertValidationFailure(underTest.validateNumaCompatibility(vm, vm.getvNumaNodeList(), vdsNumaNodes),
                EngineMessage.HOST_NUMA_NOT_SUPPORTED);
    }

    @Test
    public void shouldDetectZeroHostNodes() {
        vdsNumaNodes.clear();
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }

    @Test
    public void shouldDetectInsufficientMemory() {
        vm.setNumaTuneMode(NumaTuneMode.STRICT);
        vmNumaNodes.get(0).setMemTotal(1000);
        vdsNumaNodes.get(0).setMemTotal(500);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_MEMORY_ERROR);
    }

    @Test
    public void shouldDetectTooMuchVmNumaNodes() {
        vm.setNumaTuneMode(NumaTuneMode.PREFERRED);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE);
    }

    @Test
    public void shouldDetectTooMuchHostNodes() {
        vm.setvNumaNodeList(Collections.singletonList(createVmNumaNode(1, vdsNumaNodes)));
        vm.setNumaTuneMode(NumaTuneMode.PREFERRED);
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
        vm.setNumaTuneMode(NumaTuneMode.PREFERRED);
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
        vm.setNumaTuneMode(NumaTuneMode.STRICT);
        vmNumaNodes.get(0).setMemTotal(1000);
        vdsNumaNodes.get(0).setMemTotal(2000);
        assertTrue(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldDetectMissingRequiredHostNumaNodes() {
        vdsNumaNodes.remove(0);
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_HOST_NODE_INVALID_INDEX);
    }

    @Test
    public void shouldOnlyDoWithPinnedToHostMigrationSupport() {
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);

        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
    }

    @Test
    public void shouldNotDoWithoutPinnedHost() {
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);

        vm.setDedicatedVmForVdsList(new ArrayList<>());
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()), EngineMessage
                .ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
    }

    @Test
    public void shouldNotDoWithTwoPinnedHost() {
        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        assertValidationFailure(underTest.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.ACTION_TYPE_FAILED_VM_PINNED_TO_MULTIPLE_HOSTS);
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
                EngineMessage.VM_NUMA_NODE_MEMORY_ERROR);
    }

    private void assertValidationFailure(ValidationResult validationResult, EngineMessage engineMessage) {
        assertThat(validationResult, failsWith(engineMessage));
    }
}
