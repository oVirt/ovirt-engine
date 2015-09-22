package org.ovirt.engine.core.bll.numa.vm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;

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
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class AddVmNumaNodesCommandTest {

    private VmNumaNodeOperationParameters parameters;

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

    private VdsNumaNode vdsNumaNode;

    private VmNumaNode vmNumaNode;

    private VM vm;

    @Before
    public void setUp() throws Exception {

        SimpleDependecyInjector.getInstance().bind(DbFacade.class, dbFacade);

        vdsNumaNode = new VdsNumaNode();
        vdsNumaNode.setIndex(1);
        vdsNumaNode.setId(Guid.newGuid());
        when(vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(any(Guid.class))).thenReturn(Arrays.asList(vdsNumaNode));

        vmNumaNode = new VmNumaNode();
        vmNumaNode.setIndex(1);
        when(vmNumaNodeDao.getAllVdsNumaNodeByVdsId(any(Guid.class))).thenReturn(Arrays.<VdsNumaNode>asList(vmNumaNode));
        vmNumaNode.setVdsNumaNodeList(Arrays.asList(new Pair<>(vdsNumaNode.getId(), new Pair<>(true, vdsNumaNode.getIndex()))));

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid()));
        when(vmDao.get(eq(vm.getId()))).thenReturn(vm);

        parameters = new VmNumaNodeOperationParameters(vm.getId(), vmNumaNode);
        parameters.setDedicatedHostList(null);
        parameters.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        parameters.setNumaTuneMode(NumaTuneMode.INTERLEAVE);
    }

    @Test
    public void canDoWithPinnedHostOnVm(){
        final AddVmNumaNodesCommand command = mockedCommand();

        assertThat(command.canDoAction(), is(true));
    }

    @Test
    public void canOnlyDoWithPinnedToHostPolicy(){
        final AddVmNumaNodesCommand command = mockedCommand();

        parameters.setMigrationSupport(MigrationSupport.MIGRATABLE);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
    }

    @Test
    public void canDoWithPinnedHostOnParameters(){
        final AddVmNumaNodesCommand command = mockedCommand();

        parameters.setDedicatedHostList(Arrays.asList(Guid.newGuid()));
        vm.setDedicatedVmForVdsList(new ArrayList<Guid>());
        assertThat(command.canDoAction(), is(true));
    }

    @Test
    public void canNotDoWithoutPinnedHost(){
        final AddVmNumaNodesCommand command = mockedCommand();

        parameters.setDedicatedHostList(null);
        vm.setDedicatedVmForVdsList(new ArrayList<Guid>());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
    }

    @Test
    public void canNotDoWithTwoPinnedHost(){
        final AddVmNumaNodesCommand command = mockedCommand();

        vm.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_PINNED_TO_MULTIPLE_HOSTS);
    }

    private AddVmNumaNodesCommand<VmNumaNodeOperationParameters> mockedCommand(){
        final AddVmNumaNodesCommand<VmNumaNodeOperationParameters> command = spy(new AddVmNumaNodesCommand<>(parameters));
        when(command.getDbFacade()).thenReturn(dbFacade);
        when(dbFacade.getVmNumaNodeDao()).thenReturn(vmNumaNodeDao);
        when(dbFacade.getVdsNumaNodeDao()).thenReturn(vdsNumaNodeDao);
        when(dbFacade.getVmDao()).thenReturn(vmDao);
        return command;
    }
}
