package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AddVmPoolWithVmsCommandTest extends CommonVmPoolWithVmsCommandTestAbstract {

    @SuppressWarnings("serial")
    @Override
    protected AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> createCommand() {
        AddVmPoolWithVmsParameters param =
                new AddVmPoolWithVmsParameters(vmPools, testVm, VM_COUNT, DISK_SIZE);
        param.setStorageDomainId(firstStorageDomainId);
        return spy(new AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters>(param) {
            @Override
            protected void initTemplate() {
                // do nothing - is done here and not with mockito since it's called in the ctor
            }
        });
    }

    @Test
    public void validateCanDoAction() {
        assertTrue(command.canDoAction());
    }

    @Test
    public void validateFreeSpaceOnDestinationDomains() {
        assertTrue(command.checkFreeSpaceAndTypeOnDestDomains());
    }

    @Test
    public void validateMultiDisksWithNotEnoughSpaceOnDomains() {
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, 95);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void validateNoFreeSpaceOnDomains() {
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, 100);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }
}
