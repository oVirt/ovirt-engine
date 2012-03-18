package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Backend.class, Config.class, VmHandler.class, VmTemplateHandler.class})
public class UpdateVmPoolWithVmsCommandTest extends CommonVmPoolWithVmsCommandTestAbstract {
    /**
     * The command under test.
     */
    private UpdateVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> command;

    protected UpdateVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> createCommand() {
        AddVmPoolWithVmsParameters param = new AddVmPoolWithVmsParameters(vmPools, testVm,
                VM_COUNT, DISK_SIZE);
        param.setStorageDomainId(firstStorageDomainId);
        command = new UpdateVmPoolWithVmsCommand<AddVmPoolWithVmsParameters>(param);
        return spy(command);
    }

    public UpdateVmPoolWithVmsCommandTest() {
        super();
    }

    @Test
    public void validateCanDoAction() {
        setupMocks();
        mockVMPoolDAO();
        createCommand();
        assertTrue(command.canDoAction());
    }

    private void mockVMPoolDAO() {
        when(vmPoolDAO.get(vmPoolId)).thenReturn(vmPools);
    }
}
