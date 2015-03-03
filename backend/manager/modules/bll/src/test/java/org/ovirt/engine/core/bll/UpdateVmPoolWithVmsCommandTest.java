package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;

public class UpdateVmPoolWithVmsCommandTest extends CommonVmPoolWithVmsCommandTestAbstract {

    @Override
    protected UpdateVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> createCommand() {
        AddVmPoolWithVmsParameters param = new AddVmPoolWithVmsParameters(vmPools, testVm,
                VM_COUNT, DISK_SIZE);
        param.setStorageDomainId(firstStorageDomainId);
        UpdateVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> command =
                spy(new UpdateVmPoolWithVmsCommand<AddVmPoolWithVmsParameters>(param) {
            @Override
            protected void initTemplate() {
                // do nothing - is done here and not with mockito since it's called in the ctor
            }
        });
        return command;
    }

    @Test
    public void validateCanDoAction() {
        mockVMPoolDAO();
        setupForStorageTests();
        assertTrue(command.canDoAction());
    }

    private void mockVMPoolDAO() {
        when(vmPoolDAO.get(vmPoolId)).thenReturn(vmPools);
    }
}
