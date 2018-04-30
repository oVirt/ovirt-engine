package org.ovirt.engine.core.bll.numa.vm;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNodeWithId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateVmNumaNodesCommandTest
        extends AbstractVmNumaNodeCommandTestBase<UpdateVmNumaNodesCommand<VmNumaNodeOperationParameters>> {

    @Override
    protected Function<VmNumaNodeOperationParameters, UpdateVmNumaNodesCommand<VmNumaNodeOperationParameters>>
        commandCreator() {
        return p -> new UpdateVmNumaNodesCommand<>(p, null);
    }

    protected void initNumaNodes() {
        vdsNumaNodes = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2), createVdsNumaNode(3)));
        existingNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNodeWithId(0, NODE_ID_0),
                createVmNumaNodeWithId(1, NODE_ID_1), createVmNumaNode(2)));
        paramNumaNodes.addAll(Arrays.asList(createVmNumaNodeWithId(0, NODE_ID_0),
                createVmNumaNodeWithId(1, NODE_ID_1)));
    }

    private static final Guid NODE_ID_0 = Guid.newGuid();
    private static final Guid NODE_ID_1 = Guid.newGuid();

    @Test
    public void canUpdateNumaConfigurationWithVmFromParams() {
        vm.setvNumaNodeList(existingNumaNodes);
        mockCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massUpdateNumaNode(eq(paramNumaNodes));
    }

    @Test
    public void canUpdateNumaConfigurationWithVmFromDb() {
        mockCommandWithVmFromDb();
        command.executeCommand();
        verify(vmNumaNodeDao).massUpdateNumaNode(eq(paramNumaNodes));
    }

    @Test
    public void canUpdateNumaPinning() {
        paramNumaNodes.clear();
        paramNumaNodes.add(createVmNumaNode(1, vdsNumaNodes));
        mockCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massUpdateNumaNode(eq(paramNumaNodes));
    }

    @Test
    public void canDetectMissingRequiredHostNumaNodes() {
        paramNumaNodes.clear();
        paramNumaNodes.add(createVmNumaNodeWithId(0, vdsNumaNodes, NODE_ID_0));
        vm.setvNumaNodeList(existingNumaNodes);
        vdsNumaNodes.remove(0);
        mockCommandWithVmFromParams();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_NUMA_NODE_HOST_NODE_INVALID_INDEX);
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
    public void canUpdateNumaNodesWithArbitraryIndex() {
        paramNumaNodes.clear();
        paramNumaNodes.addAll(Arrays.asList(createVmNumaNode(0), createVmNumaNode(2)));
        mockCommandWithVmFromParams();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void canDetectMissingVM() {
        when(vmDao.get(eq(vm.getId()))).thenReturn(null);
        mockCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void shouldRunValidation() {
        vdsNumaNodes.clear();
        mockCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }
}
