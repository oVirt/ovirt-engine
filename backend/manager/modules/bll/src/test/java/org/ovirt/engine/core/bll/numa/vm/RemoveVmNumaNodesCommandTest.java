package org.ovirt.engine.core.bll.numa.vm;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
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
public class RemoveVmNumaNodesCommandTest
        extends AbstractVmNumaNodeCommandTestBase<RemoveVmNumaNodesCommand<VmNumaNodeOperationParameters>> {

    @Override
    protected Function<VmNumaNodeOperationParameters, RemoveVmNumaNodesCommand<VmNumaNodeOperationParameters>>
        commandCreator() {
        return p -> new RemoveVmNumaNodesCommand<>(p, null);
    }

    protected void initNumaNodes() {
        vdsNumaNodes = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2)));
        existingNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNodeWithId(0, NODE_ID_0),
                createVmNumaNodeWithId(1, NODE_ID_1), createVmNumaNodeWithId(2, NODE_ID_2)));
        paramNumaNodes.addAll(
                Arrays.asList(createVmNumaNodeWithId(0, NODE_ID_0), createVmNumaNodeWithId(1, NODE_ID_1)));
    }

    private static final Guid NODE_ID_0 = Guid.newGuid();
    private static final Guid NODE_ID_1 = Guid.newGuid();
    private static final Guid NODE_ID_2 = Guid.newGuid();

    @Test
    public void canRemoveNodesWithVmFromParams() {
        vm.setvNumaNodeList(existingNumaNodes);
        mockCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massRemoveNumaNodeByNumaNodeId(Arrays.asList(NODE_ID_0, NODE_ID_1));
    }

    @Test
    public void canRemoveNodesWithVmFromDb() {
        mockCommandWithVmFromDb();
        command.executeCommand();
        verify(vmNumaNodeDao).massRemoveNumaNodeByNumaNodeId(Arrays.asList(NODE_ID_0, NODE_ID_1));
    }

    @Test
    public void canDeleteNodeWithHighestIndex() {
        paramNumaNodes.clear();
        paramNumaNodes.add(createVmNumaNodeWithId(2, NODE_ID_2));
        vm.setvNumaNodeList(existingNumaNodes);
        mockCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void canNotDeleteNodeWithLowerIndex() {
        vm.setvNumaNodeList(existingNumaNodes);
        mockCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VM_NUMA_NODE_NON_CONTINUOUS_INDEX);
    }

    @Test
    public void canDeleteMultipleNodesAtOnce() {
        paramNumaNodes.clear();
        paramNumaNodes.addAll(Arrays.asList(createVmNumaNodeWithId(1, NODE_ID_1),
                createVmNumaNodeWithId(2, NODE_ID_2)));
        vm.setvNumaNodeList(existingNumaNodes);
        mockCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void canDetectMissingVM() {
        when(vmDao.get(eq(vm.getId()))).thenReturn(null);
        mockCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }
}
