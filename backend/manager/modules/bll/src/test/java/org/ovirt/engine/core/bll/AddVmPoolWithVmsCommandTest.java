package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Backend.class, Config.class, VmHandler.class, VmTemplateHandler.class})
public class AddVmPoolWithVmsCommandTest extends CommonVmPoolWithVmsCommandTestAbstract {
    /**
     * The command under test.
     */
    private AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> command;

    protected AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> createCommand() {
        AddVmPoolWithVmsParameters param = new AddVmPoolWithVmsParameters(vmPools, testVm,
                VM_COUNT, DISK_SIZE);
        param.setStorageDomainId(firstStorageDomainId);
        command = new AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters>(param);
        return spy(command);
    }

    public AddVmPoolWithVmsCommandTest() {
        super();
    }

    @Test
    public void validateCanDoAction() {
        setupMocks();
        assertTrue(createCommand().canDoAction());
    }

    @Test
    public void validateFreeSpaceOnDestinationDomains() {
        setupMocks();
        assertTrue(createCommand().checkFreeSpaceAndTypeOnDestDomains());
    }

    @Test
    public void validateMultiDisksWithNotEnoughSpaceOnDomains() {
        setupMocks();
        AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> cmd = createCommand();
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(95);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void validateNoFreeSpaceOnDomains() {
        setupMocks();
        AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> cmd = createCommand();
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(100);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void validateNoFreeSpacePctOnDomains() {
        setupMocks();
        mockGetImageDomainsListVdsCommand(2, 2);
        AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> cmd = createCommand();
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceLow)).thenReturn(50);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }
}
