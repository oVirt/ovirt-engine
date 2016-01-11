package org.ovirt.engine.core.bll.numa.vm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVdsNumaNodeDao;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVmNumaNodeDao;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.toVdsNumaNodes;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class AddVmNumaNodesCommandTest {

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

    @Before
    public void setUp() throws Exception {

        SimpleDependecyInjector.getInstance().bind(DbFacade.class, dbFacade);

        vdsNumaNodes = new ArrayList<>(Arrays.asList(createVdsNumaNode(1), createVdsNumaNode(2), createVdsNumaNode(3)));
        existingNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(1), createVmNumaNode(2)));
        newNumaNodes = new ArrayList<>(Arrays.asList(createVmNumaNode(1), createVmNumaNode(2)));
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
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massSaveNumaNode(eq(toVdsNumaNodes(newNumaNodes)), any(Guid.class), any(Guid.class));
    }

    @Test
    public void canSetNumaConfigurationWithVmFromDb() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();
        command.executeCommand();
        verify(vmNumaNodeDao).massSaveNumaNode(eq(toVdsNumaNodes(newNumaNodes)), any(Guid.class), any(Guid.class));
    }

    @Test
    public void canSetNumaPinning() {
        newNumaNodes = Arrays.asList(createVmNumaNode(1, vdsNumaNodes));
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromParams();
        command.executeCommand();
        verify(vmNumaNodeDao).massSaveNumaNode(eq(toVdsNumaNodes(newNumaNodes)), any(Guid.class), any(Guid.class));
    }

    @Test
    public void canDetectMissingVM() {
        when(vmDao.get(eq(vm.getId()))).thenReturn(null);
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void canDetectZeroHostNodesWithVmFromParams() {
        vdsNumaNodes.clear();
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromParams();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }

    @Test
    public void canDetectMissingRequiredHostNumaNodes() {
        existingNumaNodes.set(0, createVmNumaNode(1, vdsNumaNodes));
        vdsNumaNodes.remove(0);
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromParams();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.VM_NUMA_NODE_HOST_NODE_INVALID_INDEX);
    }

    @Test
    public void canDetectZeroHostNodesWithVmFromDb() {
        vdsNumaNodes.clear();
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
    }

    @Test
    public void canDoWithPinnedHostOnVm() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();

        assertThat(command.canDoAction(), is(true));
    }

    @Test
    public void canOnlyDoWithPinnedToHostPolicy() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();

        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
    }

    @Test
    public void canNotDoWithoutPinnedHost() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);

        vm.setDedicatedVmForVdsList(new ArrayList<Guid>());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
    }

    @Test
    public void canNotDoWithTwoPinnedHost() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();

        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_PINNED_TO_MULTIPLE_HOSTS);
    }

    @Test
    public void canCreateAsMuchNumaNodesAsVirtualCores() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();

        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(4);
        assertThat(command.canDoAction(), is(true));
    }

    @Test
    public void canCreateLessNumaNodesAsVirtualCores() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();

        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(5);
        assertThat(command.canDoAction(), is(true));
    }

    @Test
    public void failCreateMoreNumaNodesThanVirtualCoresWithVmFromDb() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromDb();

        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(3);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, EngineMessage.VM_NUMA_NODE_MORE_NODES_THAN_CPUS);
    }

    @Test
    public void failCreateMoreNumaNodesThanVirtualCoresWithVmFromParams() {
        final AddVmNumaNodesCommand command = mockedCommandWithVmFromParams();
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(3);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, EngineMessage.VM_NUMA_NODE_MORE_NODES_THAN_CPUS);
    }

    private AddVmNumaNodesCommand<VmNumaNodeOperationParameters> mockedCommandWithVmFromParams() {
        return mockedCommand(new VmNumaNodeOperationParameters(vm, newNumaNodes));
    }

    private AddVmNumaNodesCommand<VmNumaNodeOperationParameters> mockedCommandWithVmFromDb() {
        return mockedCommand(new VmNumaNodeOperationParameters(vm.getId(), newNumaNodes));
    }

    private AddVmNumaNodesCommand<VmNumaNodeOperationParameters> mockedCommand(VmNumaNodeOperationParameters parameters) {
        final AddVmNumaNodesCommand<VmNumaNodeOperationParameters> command =
                spy(new AddVmNumaNodesCommand<>(parameters));
        DbFacade.setInstance(dbFacade);
        when(dbFacade.getVmNumaNodeDao()).thenReturn(vmNumaNodeDao);
        when(dbFacade.getVdsNumaNodeDao()).thenReturn(vdsNumaNodeDao);
        when(dbFacade.getVmDao()).thenReturn(vmDao);
        command.init();
        return command;
    }
}
