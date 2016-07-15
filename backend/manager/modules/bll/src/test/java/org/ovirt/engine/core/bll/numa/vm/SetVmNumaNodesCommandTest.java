package org.ovirt.engine.core.bll.numa.vm;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class SetVmNumaNodesCommandTest
        extends AbstractVmNumaNodeCommandTestBase<SetVmNumaNodesCommand<VmNumaNodeOperationParameters>> {

    private List<Guid> existingNumaNodeIds;

    @Override
    protected Function<VmNumaNodeOperationParameters, SetVmNumaNodesCommand<VmNumaNodeOperationParameters>>
        commandCreator() {
        return p -> new SetVmNumaNodesCommand<>(p, null);
    }

    protected void initNumaNodes() {
        vdsNumaNodes = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2)));
        existingNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(0), createVmNumaNode(1)));
        existingNumaNodeIds = existingNumaNodes.stream().map(VmNumaNode::getId).collect(Collectors.toList());
        paramNumaNodes.addAll(Arrays.asList(createVmNumaNode(0), createVmNumaNode(1)));
    }

    @Test
    public void canSetNumaConfigurationWithVmFromParams() {
        mockCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massRemoveNumaNodeByNumaNodeId(eq(existingNumaNodeIds));
        verify(vmNumaNodeDao).massSaveNumaNode(eq(paramNumaNodes), any(Guid.class));
    }

    @Test
    public void canSetNumaConfigurationWithVmFromDb() {
        mockCommandWithVmFromDb();
        command.executeCommand();
        verify(vmNumaNodeDao).massRemoveNumaNodeByNumaNodeId(eq(existingNumaNodeIds));
        verify(vmNumaNodeDao).massSaveNumaNode(eq(paramNumaNodes), any(Guid.class));
    }

    @Test
    public void canSetNumaPinning() {
        paramNumaNodes.clear();
        paramNumaNodes.add(createVmNumaNode(1, vdsNumaNodes));
        mockCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massRemoveNumaNodeByNumaNodeId(eq(existingNumaNodeIds));
        verify(vmNumaNodeDao).massSaveNumaNode(eq(paramNumaNodes), any(Guid.class));
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
