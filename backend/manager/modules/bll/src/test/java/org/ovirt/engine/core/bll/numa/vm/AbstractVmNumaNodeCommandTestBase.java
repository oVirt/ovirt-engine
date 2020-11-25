package org.ovirt.engine.core.bll.numa.vm;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVdsDao;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVdsNumaNodeDao;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.mockVmNumaNodeDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public abstract class AbstractVmNumaNodeCommandTestBase
        <T extends AbstractVmNumaNodeCommand<VmNumaNodeOperationParameters>> extends BaseCommandTest {

    @Mock
    protected VmNumaNodeDao vmNumaNodeDao;

    @Mock
    protected VmDao vmDao;

    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;

    @Mock
    private VdsDao vdsDao;

    @InjectMocks
    private NumaValidator numaValidator;

    protected VM vm;
    protected List<VmNumaNode> existingNumaNodes;
    protected List<VdsNumaNode> vdsNumaNodes;
    protected List<VmNumaNode> paramNumaNodes = new ArrayList<>();

    @Spy
    @InjectMocks
    protected T command = commandCreator().apply(new VmNumaNodeOperationParameters((VM) null, paramNumaNodes));

    protected abstract Function<VmNumaNodeOperationParameters, T> commandCreator();

    protected abstract void initNumaNodes();

    @BeforeEach
    public void setUp() {
        initNumaNodes();
        mockVdsNumaNodeDao(vdsNumaNodeDao, vdsNumaNodes);
        mockVmNumaNodeDao(vmNumaNodeDao, existingNumaNodes);
        mockVdsDao(vdsDao);

        doReturn(numaValidator).when(command).getNumaValidator();

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setDedicatedVmForVdsList(Collections.singletonList(Guid.newGuid()));
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(4);
        vm.setVmMemSizeMb(4000);
        vm.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vm.getvNumaNodeList().forEach(node -> node.setNumaTuneMode(NumaTuneMode.INTERLEAVE));
        when(vmDao.get(eq(vm.getId()))).thenReturn(vm);
    }

    @AfterEach
    public void tearDown() {
        paramNumaNodes.clear();
        command.getParameters().setVm(null);
        command.getParameters().setVmId(null);
    }

    protected void mockCommandWithVmFromParams() {
        command.getParameters().setVm(vm);
        command.init();
    }

    protected void mockCommandWithVmFromDb() {
        command.setVmId(vm.getId());
        command.getParameters().setVmId(vm.getId());
        command.init();
    }
}
