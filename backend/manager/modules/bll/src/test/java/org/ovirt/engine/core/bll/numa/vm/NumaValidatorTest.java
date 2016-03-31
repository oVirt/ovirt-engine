package org.ovirt.engine.core.bll.numa.vm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVdsNumaNodeDao;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;

@RunWith(MockitoJUnitRunner.class)
public class NumaValidatorTest {

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.SupportNUMAMigration, false)
    );

    @Mock
    DbFacade dbFacade;

    @Mock
    VdsNumaNodeDao vdsNumaNodeDao;

    private ArrayList<VdsNumaNode> vdsNumaNodes;
    private VM vm;
    private ArrayList<VmNumaNode> vmNumaNodes;

    @Before
    public void setUp() throws Exception {

        SimpleDependencyInjector.getInstance().bind(DbFacade.class, dbFacade);
        DbFacade.setInstance(dbFacade);
        when(dbFacade.getVdsNumaNodeDao()).thenReturn(vdsNumaNodeDao);

        vdsNumaNodes = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2), createVdsNumaNode(3)));
        vmNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(0, vdsNumaNodes), createVmNumaNode(1)));
        mockVdsNumaNodeDao(vdsNumaNodeDao, vdsNumaNodes);

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid()));
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(2);
        vm.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vm.setNumaTuneMode(NumaTuneMode.INTERLEAVE);
        vm.setvNumaNodeList(vmNumaNodes);
    }

    @Test
    public void shouldSetNumaPinning() {
        assertTrue(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldHandleNoNumaNodes() {
        vm.setvNumaNodeList(null);
        assertTrue(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldDetectHostWihtoutNumaSupport() {
        vdsNumaNodes = new ArrayList(Arrays.asList(createVdsNumaNode(1)));
        assertValidationFailure(NumaValidator.validateNumaCompatibility(vm, vm.getvNumaNodeList(), vdsNumaNodes),
                EngineMessage.HOST_NUMA_NOT_SUPPORTED);
    }

    @Test
    public void shouldDetectZeroHostNodes() {
        vdsNumaNodes.clear();
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }

    @Test
    public void shouldDetectInsufficientMemory() {
        vm.setNumaTuneMode(NumaTuneMode.STRICT);
        vmNumaNodes.get(0).setMemTotal(1000);
        vdsNumaNodes.get(0).setMemTotal(500);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_MEMORY_ERROR);
    }

    @Test
    public void shouldDetectTooMuchVmNumaNodes() {
        vm.setNumaTuneMode(NumaTuneMode.PREFERRED);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE);
    }

    @Test
    public void shouldDetectTooMuchHostNodes() {
        vm.setvNumaNodeList(Arrays.asList(createVmNumaNode(1, vdsNumaNodes)));
        vm.setNumaTuneMode(NumaTuneMode.PREFERRED);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE);
    }

    @Test
    public void shouldDetectDuplicateNodeIndex() {
        vmNumaNodes.get(0).setIndex(1);
        vmNumaNodes.get(1).setIndex(1);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_INDEX_DUPLICATE);
    }

    @Test
    public void shouldDetectNonContinuousNodeIndices() {
        vmNumaNodes.get(0).setIndex(0);
        vmNumaNodes.get(1).setIndex(2);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_NON_CONTINUOUS_INDEX);
    }

    @Test
    public void shouldDetectIndicesNotStartingWithZero() {
        vmNumaNodes.get(0).setIndex(2);
        vmNumaNodes.get(1).setIndex(3);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_NON_CONTINUOUS_INDEX);
    }

    @Test
    public void shouldValidateSingleNodePinning() {
        vm.setvNumaNodeList(Arrays.asList(createVmNumaNode(0, Arrays.asList(createVdsNumaNode(1)))));
        vm.setNumaTuneMode(NumaTuneMode.PREFERRED);
        assertTrue(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldDetectMissingPinningEntry() {
        vm.getvNumaNodeList().get(0).getVdsNumaNodeList().get(0).getSecond().setSecond(null);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_PINNED_INDEX_ERROR);
    }

    @Test
    public void shouldDetectSufficientMemory() {
        vm.setNumaTuneMode(NumaTuneMode.STRICT);
        vmNumaNodes.get(0).setMemTotal(1000);
        vdsNumaNodes.get(0).setMemTotal(2000);
        assertTrue(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldDetectMissingRequiredHostNumaNodes() {
        vdsNumaNodes.remove(0);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_HOST_NODE_INVALID_INDEX);
    }

    @Test
    public void shouldOnlyDoWithPinnedToHostMigrationSupport() {
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);

        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
    }

    @Test
    public void shouldNotDoWithoutPinnedHost() {
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);

        vm.setDedicatedVmForVdsList(new ArrayList<>());
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()), EngineMessage
                .ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
    }

    @Test
    public void shouldNotDoWithTwoPinnedHost() {
        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.ACTION_TYPE_FAILED_VM_PINNED_TO_MULTIPLE_HOSTS);
    }

    @Test
    public void shouldCreateAsMuchNumaNodesAsVirtualCores() {
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(2);
        assertTrue(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void shouldCreateLessNumaNodesAsVirtualCores() {
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(3);
        assertTrue(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()).isValid());
    }

    @Test
    public void failCreateMoreNumaNodesThanVirtualCores() {
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(1);
        assertValidationFailure(NumaValidator.checkVmNumaNodesIntegrity(vm, vm.getvNumaNodeList()),
                EngineMessage.VM_NUMA_NODE_MORE_NODES_THAN_CPUS);
    }

    private void assertValidationFailure(ValidationResult validationResult, EngineMessage engineMessage) {
        assertFalse(validationResult.isValid());
        assertTrue(String.format("Expected %s but got %s", engineMessage.name(), validationResult.getMessage().name()),
                validationResult.getMessage() == engineMessage);
    }
}
