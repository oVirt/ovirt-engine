package org.ovirt.engine.core.bll.numa.vm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AddVmNumaNodesCommandTest
        extends AbstractVmNumaNodeCommandTestBase<AddVmNumaNodesCommand<VmNumaNodeOperationParameters>> {

    @Override
    protected Function<VmNumaNodeOperationParameters, AddVmNumaNodesCommand<VmNumaNodeOperationParameters>>
        commandCreator() {
        return p -> new AddVmNumaNodesCommand<>(p, null);
    }

    protected void initNumaNodes() {
        vdsNumaNodes = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2), createVdsNumaNode(3)));
        existingNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(0), createVmNumaNode(1)));
        paramNumaNodes.addAll(Arrays.asList(createVmNumaNode(2), createVmNumaNode(3)));
    }

    @Test
    public void canSetNumaConfigurationWithVmFromParams() {
        mockCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massSaveNumaNode(eq(paramNumaNodes), any());
    }

    @Test
    public void canSetNumaConfigurationWithVmFromDb() {
        mockCommandWithVmFromDb();
        command.executeCommand();
        verify(vmNumaNodeDao).massSaveNumaNode(eq(paramNumaNodes), any());
    }

    @Test
    public void canSetNumaPinning() {
        paramNumaNodes.clear();
        paramNumaNodes.add(createVmNumaNode(1, vdsNumaNodes));
        mockCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massSaveNumaNode(eq(paramNumaNodes), any());
    }

    @Test
    public void canDetectMissingVM() {
        when(vmDao.get(eq(vm.getId()))).thenReturn(null);
        mockCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void canDetectZeroHostNodesWithVmFromParams() {
        vdsNumaNodes.clear();
        mockCommandWithVmFromParams();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }

    @Test
    public void canDetectMissingRequiredHostNumaNodes() {
        existingNumaNodes.set(0, createVmNumaNode(0, vdsNumaNodes));
        vdsNumaNodes.remove(0);
        mockCommandWithVmFromParams();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_NUMA_NODE_HOST_NODE_INVALID_INDEX);
    }

    @Test
    public void canDetectZeroHostNodesWithVmFromDb() {
        vdsNumaNodes.clear();
        mockCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }

    @Test
    public void validateWithPinnedHostOnVm() {
        mockCommandWithVmFromDb();
        assertThat(command.validate()).isTrue();
    }

    @Test
    public void canNotDoWithoutPinnedHost() {
        mockCommandWithVmFromDb();
        vm.setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);

        vm.setDedicatedVmForVdsList(new ArrayList<>());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_AT_LEAST_ONE_HOST);
    }

    @Test
    public void canDoWithTwoPinnedHost() {
        mockCommandWithVmFromDb();
        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        assertThat(command.validate()).isTrue();
    }

    @Test
    public void canCreateAsMuchNumaNodesAsVirtualCores() {
        mockCommandWithVmFromDb();
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(4);
        assertThat(command.validate()).isTrue();
    }

    @Test
    public void canCreateLessNumaNodesAsVirtualCores() {
        mockCommandWithVmFromDb();
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(5);
        assertThat(command.validate()).isTrue();
    }

    @Test
    public void failCreateMoreNumaNodesThanVirtualCoresWithVmFromDb() {
        mockCommandWithVmFromDb();
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(3);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VM_NUMA_NODE_MORE_NODES_THAN_CPUS);
    }

    @Test
    public void failCreateMoreNumaNodesThanVirtualCoresWithVmFromParams() {
        mockCommandWithVmFromParams();
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(3);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VM_NUMA_NODE_MORE_NODES_THAN_CPUS);
    }

    @Test
    public void canDetectDuplicateNumaNodes() {
        paramNumaNodes.clear();
        paramNumaNodes.addAll(Arrays.asList(createVmNumaNode(10), createVmNumaNode(10)));
        mockCommandWithVmFromParams();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_NUMA_NODE_INDEX_DUPLICATE);
    }

    @Test
    public void canDetectNonContinuousNumaNodeIndices() {
        paramNumaNodes.clear();
        paramNumaNodes.addAll(Arrays.asList(createVmNumaNode(2), createVmNumaNode(4)));
        mockCommandWithVmFromParams();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_NUMA_NODE_NON_CONTINUOUS_INDEX);
    }

}
