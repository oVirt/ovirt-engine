package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;

public class UpdateVmPoolCommandTest extends CommonVmPoolCommandTestAbstract {

    @Override
    protected UpdateVmPoolCommand<AddVmPoolParameters> createCommand() {
        AddVmPoolParameters param = new AddVmPoolParameters(vmPools, testVm, VM_COUNT);
        param.setStorageDomainId(firstStorageDomainId);
        return new UpdateVmPoolCommand<>(param, null);
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
