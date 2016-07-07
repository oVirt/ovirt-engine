package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;

public class UpdateVmPoolCommandTest extends CommonVmPoolCommandTestAbstract {

    @Override
    protected UpdateVmPoolCommand<AddVmPoolParameters> createCommand() {
        AddVmPoolParameters param = new AddVmPoolParameters(vmPools, testVm,
                VM_COUNT);
        param.setStorageDomainId(firstStorageDomainId);
        UpdateVmPoolCommand<AddVmPoolParameters> command =
                spy(new UpdateVmPoolCommand<AddVmPoolParameters>(
                        param, CommandContext.createContext(param.getSessionId())) {

                    @Override
                    protected void initUser() {
                    }

                    @Override
                    protected void initTemplate() {
                        // do nothing - is done here and not with mockito since it's called in the ctor
                    }
                });
        return command;
    }

    @Test
    public void validate() {
        mockVMPoolDao();
        setupForStorageTests();
        assertTrue(command.validate());
    }

    private void mockVMPoolDao() {
        when(vmPoolDao.get(vmPoolId)).thenReturn(vmPools);
    }
}
