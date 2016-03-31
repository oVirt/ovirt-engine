package org.ovirt.engine.core.bll.numa.vm;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVdsNumaNodeDao;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVmNumaNodeDao;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
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
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

public class SetVmNumaNodesCommandTest extends BaseCommandTest {

    @Mock
    private DbFacade dbFacade;

    @Mock
    private VmNumaNodeDao vmNumaNodeDao;

    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;

    @Mock
    private VmDao vmDao;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.SupportNUMAMigration, false)
    );

    private VM vm;
    private List<VmNumaNode> existingNumaNodes;
    private List<VdsNumaNode> vdsNumaNodes;
    private List<VmNumaNode> newNumaNodes;
    private List<Guid> existingNumaNodeIds;

    @Before
    public void setUp() throws Exception {
        super.setUpSessionDataContainer();

        SimpleDependencyInjector.getInstance().bind(DbFacade.class, dbFacade);
        DbFacade.setInstance(dbFacade);

        vdsNumaNodes = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2)));
        existingNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(0), createVmNumaNode(1)));
        existingNumaNodeIds = existingNumaNodes.stream().map(VmNumaNode::getId).collect(Collectors.toList());
        newNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(0), createVmNumaNode(1)));
        mockVdsNumaNodeDao(vdsNumaNodeDao, vdsNumaNodes);
        mockVmNumaNodeDao(vmNumaNodeDao, existingNumaNodes);

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid()));
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(4);
        vm.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vm.setNumaTuneMode(NumaTuneMode.INTERLEAVE);
        when(vmDao.get(eq(vm.getId()))).thenReturn(vm);
    }

    @Test
    public void canSetNumaConfigurationWithVmFromParams() {
        final SetVmNumaNodesCommand command = mockedCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massRemoveNumaNodeByNumaNodeId(eq(existingNumaNodeIds));
        verify(vmNumaNodeDao).massSaveNumaNode(eq(newNumaNodes), any(Guid.class), any(Guid.class));
    }

    @Test
    public void canSetNumaConfigurationWithVmFromDb() {
        final SetVmNumaNodesCommand command = mockedCommandWithVmFromDb();
        command.executeCommand();
        verify(vmNumaNodeDao).massRemoveNumaNodeByNumaNodeId(eq(existingNumaNodeIds));
        verify(vmNumaNodeDao).massSaveNumaNode(eq(newNumaNodes), any(Guid.class), any(Guid.class));
    }

    @Test
    public void canSetNumaPinning() {
        newNumaNodes = Arrays.asList(createVmNumaNode(1, vdsNumaNodes));
        final SetVmNumaNodesCommand command = mockedCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massRemoveNumaNodeByNumaNodeId(eq(existingNumaNodeIds));
        verify(vmNumaNodeDao).massSaveNumaNode(eq(newNumaNodes), any(Guid.class), any(Guid.class));
    }

    @Test
    public void canDetectMissingVM() {
        when(vmDao.get(eq(vm.getId()))).thenReturn(null);
        final SetVmNumaNodesCommand command = mockedCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void shouldRunValidation() {
        vdsNumaNodes.clear();
        final SetVmNumaNodesCommand command = mockedCommandWithVmFromDb();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }

    private SetVmNumaNodesCommand<VmNumaNodeOperationParameters> mockedCommandWithVmFromParams() {
        return mockedCommand(new VmNumaNodeOperationParameters(vm, newNumaNodes));
    }

    private SetVmNumaNodesCommand<VmNumaNodeOperationParameters> mockedCommandWithVmFromDb() {
        return mockedCommand(new VmNumaNodeOperationParameters(vm.getId(), newNumaNodes));
    }

    private SetVmNumaNodesCommand<VmNumaNodeOperationParameters> mockedCommand(VmNumaNodeOperationParameters
            parameters) {
        final SetVmNumaNodesCommand<VmNumaNodeOperationParameters> command =
                spy(new SetVmNumaNodesCommand<>(parameters, null));
        when(command.getDbFacade()).thenReturn(dbFacade);
        when(dbFacade.getVmNumaNodeDao()).thenReturn(vmNumaNodeDao);
        when(dbFacade.getVdsNumaNodeDao()).thenReturn(vdsNumaNodeDao);
        when(dbFacade.getVmDao()).thenReturn(vmDao);
        command.init();
        return command;
    }
}
